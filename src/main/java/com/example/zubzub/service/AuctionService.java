package com.example.zubzub.service;

import com.example.zubzub.component.Broadcaster;
import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.entity.*;
import com.example.zubzub.event.AuctionCreatedEvent;
import com.example.zubzub.mapper.AuctionMapper;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.BidHistoryRepository;
import com.example.zubzub.repository.MemberRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final AuctionSchedulerService auctionSchedulerService;
    private final BidHistoryRepository bidHistoryRepository;
    private final Broadcaster broadcaster;
    private final ApplicationEventPublisher eventPublisher;

    // 실시간성을 위한 캐시 사용
    private final ConcurrentHashMap<Long, Auction> cache = new ConcurrentHashMap<>();

    // CREATE
    @Transactional
    public AuctionResDto createAuction(AuctionCreateDto dto) throws SchedulerException {
        Member seller = memberRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Auction auction = AuctionMapper.convertAuctionDtoToEntity(dto, seller);

        // 메이저에서 임시 시작 종료시간 넣기
        if (auction.getAuctionType() == AuctionType.MAJOR) {
            auction.setStartTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
            auction.setEndTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        }

        if (auction.getEndTime().isBefore(auction.getStartTime()))
            throw new IllegalArgumentException("시작 시간이 종료 시간 이후입니다.");

        if (auction.getAuctionType() == AuctionType.MAJOR) {
            // 경매생성시 자동으로 승인대기 상태로 설정 (DB에서 넣어줘도 될 듯함)
            auction.setAuctionStatus(AuctionStatus.PENDING);
        } else if (auction.getAuctionType() == AuctionType.MINOR) {
            // 경매생성시 자동으로 경매대기 상태로 설정 (DB에서 넣어줘도 될 듯함)
            auction.setAuctionStatus(AuctionStatus.READY);
        }

        // DB에 넣어서 ID 자동 채우기
        Auction savedAuction = auctionRepository.save(auction);
        log.info("경매 생성됨 : {}", auction.getId());

        if (savedAuction.getAuctionType() == AuctionType.MINOR) {
            eventPublisher.publishEvent(new AuctionCreatedEvent(savedAuction));
            log.info("경매 생성 이벤트 발행 : {}", savedAuction.getId());
        }

        return AuctionMapper.convertEntityToAuctionDto(savedAuction);
    }

    // READ (전체 조회)
    public List<AuctionResDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionMapper::convertEntityToAuctionDto)
                        .collect(Collectors.toList());
    }

    // READ (단건 조회)
    public AuctionResDto getAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
        return AuctionMapper.convertEntityToAuctionDto(auction);
    }

    // cache READ
    @Transactional(readOnly = true)
    public Auction getAuctionEntity(Long auctionId) {
        // 캐시에서 먼저 찾기
        log.info("캐시에서 찾기 : {}", auctionId);
        Auction auction = cache.get(auctionId);
        if (auction == null) {
            // DB 조회 (없으면 예외 던짐)
            log.info("DB에서 찾기 : {}", auctionId);
            auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
            // 캐시에 저장
            cache.put(auctionId, auction);
        }
        return auction;
    }

    // cache UPDATE
    public void updateAuction(Auction auction) {
        cache.put(auction.getId(), auction);

    }

    // DELETE
    @Transactional
    public void deleteAuction(Long auctionId) {
        auctionRepository.deleteById(auctionId);
        cache.remove(auctionId);
    }

    // 경매시작
    @Transactional
    public void startAuction(Long auctionId) {

        Auction auction = getAuctionEntity(auctionId);

        log.info("옥션 : {}", auction);

        // 최종가를 시작가로 초기화
        auction.setFinalPrice(auction.getStartPrice());

        // 경매중 상태로 설정
        auction.setAuctionStatus(AuctionStatus.ACTIVE);

        // 캐시에 업데이트
        updateAuction(auction);

        // 브로드캐스트
        broadcaster.broadcastAuction(auction);

        System.out.println("Auction " + auctionId + " 시작 처리 실행!");

        try {
            // 종료 타이머 걸기
            auctionSchedulerService.scheduleAuctionEnd(auction);
        } catch (SchedulerException e) {
            log.error("종료 타이머 지정 실패 : {}", e.getMessage());
            auction.setAuctionStatus(AuctionStatus.READY);
        }
    }

    // 경매종료
    @Transactional
    public void endAuction(Long auctionId) {

        // 경매 불러오기
        Auction auction = getAuctionEntity(auctionId);

        // 낙찰 처리
        Member winner = auction.getWinner();
        int winningBid = auction.getFinalPrice();

        if (winner != null) {
            winner.useLockedCredit(winningBid);
        }

        // 소규모경매시 입찰기록확인하여 이전입찰자들환불

        // 경매종료 상태로 설정
        auction.setAuctionStatus(AuctionStatus.COMPLETED);

        // 캐시에 업데이트
        updateAuction(auction);

        // 브로드캐스트
        broadcaster.broadcastAuction(auction);

        // DB에 업데이트
        Auction savedAuction = auctionRepository.save(auction);

        System.out.println("Auction " + auctionId + " 종료 처리 실행!");
    }

    // 마이페이지 판매목록 5개 가져오기
    public List<AuctionResDto> List5SellAuction(Long id) {
        List<Auction> auction = auctionRepository.findTop5BySellerIdOrderByEndTimeDesc(id);
        log.info("DB 조회 결과: {}", auction);
        return auction.stream().map(AuctionMapper::convertEntityToAuctionDto).toList();
    }
    // 마이페이지 낙찰목록 5개 가져오기
    public List<AuctionResDto> List5WinAuction(Long id) {
        List<Auction> auction = auctionRepository.findTop5ByWinnerIdAndAuctionStatusOrderByEndTimeDesc(id, AuctionStatus.COMPLETED);
        log.info("DB 조회 결과2: {}", auction);
        return auction.stream().map(AuctionMapper::convertEntityToAuctionDto).toList();
    }

    // 마이페이지 판매 목록 리스트 상세 조회 (페이지네이션)
    public Page<AuctionResDto> ListSellAuction(Long id, Pageable pageable) {
        Page<Auction> auction = auctionRepository.findBySellerId(id, pageable);
        return auction.map(AuctionMapper::convertEntityToAuctionDto);
    }
    // 마이페이지 낙찰 내역 리스트 상세 조회(페이지네이션)
    public Page<AuctionResDto> ListWinnerAuction(Long id, Pageable pageable) {
        Page<Auction> auction = auctionRepository.findByWinnerId(id, pageable);
        return auction.map(AuctionMapper::convertEntityToAuctionDto);
    }

    // 대규모 경매 관리자 승인
    @Transactional
    public void approveAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("해당 경매는 없습니다."));

        if (auction.getAuctionStatus() != AuctionStatus.PENDING) {
            throw new IllegalStateException("승인할 수 없는 상태입니다.");        }

        auction.setAuctionStatus(AuctionStatus.READY);
    }

    // 시작 시간, 종료시간 선정
    @Transactional
    public void setTime(Long id, LocalDateTime startTime, LocalDateTime endTime) {

        Auction auction = auctionRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 경매는 없습니다."));

        if (auction.getAuctionStatus() == AuctionStatus.ACTIVE)
            throw new IllegalStateException("경매중에는 수정할 수 없습니다.");

        if (startTime.isAfter(endTime)) {
            throw new IllegalStateException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        auction.setStartTime(startTime);
        auction.setEndTime(endTime);

        Auction savedAuction = auctionRepository.save(auction);
        log.info("시작 / 종료시간 재설정 됨 : {}", savedAuction);

        eventPublisher.publishEvent(new AuctionCreatedEvent(savedAuction));
        log.info("경매 생성 이벤트 발행 : {}", savedAuction.getId());
    }

    // 일반 경매 수정
    @Transactional
    public void updateNormalAuction(Long auctionId, AuctionCreateDto req) {
        // 해당 경매 있는지 확인
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(()-> new IllegalArgumentException("해당 경매는 없습니다."));
        // 해당 경매 판매자인지 확인(토큰 고려해야함 지금 안해)
        // 데이터 수정
        auction.setCategory(req.getCategory());
        auction.setStartPrice(req.getStartPrice());
        auction.setItemName(req.getItemName());
        auction.setItemDesc(req.getItemDesc());
        auction.setItemImg(req.getItemImg());
        auction.setStartTime(req.getStartTime());
        auction.setEndTime(req.getEndTime());
    }
    // 일반 경매 리스트 가져오기
    public Page<AuctionResDto> getMinorList(Pageable pageable) {

        Page<Auction> auction = auctionRepository.findByAuctionType(AuctionType.MINOR, pageable);
        return auction.map(AuctionMapper::convertEntityToAuctionDto);
    }

    // 프리미엄 경매 리스트 날짜별로 가져오기
    public List<AuctionResDto> getMajorList(LocalDateTime start, LocalDateTime end) {
        List<Auction> auction = auctionRepository.findByAuctionTypeAndStartTimeBetween(AuctionType.MAJOR, start, end);
        return auction.stream().map(AuctionMapper::convertEntityToAuctionDto).toList();
    }

    // 입찰 시퀸스
    @Transactional
    public void placeBid(Long auctionId, Long bidderId, int bidAmount) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매가 존재하지 않습니다."));
        Member bidder = memberRepository.findById(bidderId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 입찰 가능 여부 확인 (경매 상태)
        if (auction.getAuctionStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("현재 경매는 입찰이 불가능한 상태입니다.");
        }
        // 입찰 가능 여부 확인(크레딧)
        if (bidder.getAvailableCredit() < bidAmount) {
            throw new IllegalArgumentException("크레딧이 부족합니다.");
        }

        // 이전 최고 입찰자 환불
        Member prevBidder = auction.getWinner();
        int prevPrice = auction.getFinalPrice() != 0 ? auction.getFinalPrice() : 0;

        if (prevBidder != null && !prevBidder.equals(bidder)) {
            prevBidder.unlockCredit(prevPrice);
        }

        // 현재 입찰자 크레딧 잠금
        bidder.lockCredit(bidAmount);

        // Auction 엔티티 업데이트
        auction.setFinalPrice(bidAmount);
        auction.setWinner(bidder);
        auctionRepository.save(auction); // JPA 변경 감지로 생략 가능

        // BidHistory 기록 저장
        BidHistory bidHistory = BidHistory.builder()
                .auction(auction)
                .bidder(bidder)
                .price(bidAmount)
                .bidTime(LocalDateTime.now())
                .build();
        bidHistoryRepository.save(bidHistory);
    }

    // 최종 낙찰 시퀸스
    @Transactional
    public void finalizeAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매가 존재하지 않습니다."));

        Member winner = auction.getWinner();
        int winningBid = auction.getFinalPrice();

        if (winner != null) {
            winner.useLockedCredit(winningBid);
        }

        auction.setAuctionStatus(AuctionStatus.COMPLETED);
        auctionRepository.save(auction);
    }

    // 관리자 대규모 경매 승인 목록 불러오기
    public List<AuctionResDto> getPendingList() {
        List<Auction> auction = auctionRepository.findByAuctionStatus(AuctionStatus.PENDING);
        return auction.stream().map(AuctionMapper::convertEntityToAuctionDto).toList();
    }


}