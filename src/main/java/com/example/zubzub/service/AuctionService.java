package com.example.zubzub.service;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.entity.AuctionType;
import com.example.zubzub.entity.Member;
import com.example.zubzub.mapper.AuctionMapper;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.repository.MemberRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final AuctionSchedulerService auctionSchedulerService;
    // 실시간성을 위한 캐시 사용
    private final ConcurrentHashMap<Long, Auction> cache = new ConcurrentHashMap<>();

    // CREATE
    public Boolean createAuction(AuctionCreateDto dto) {
        Member seller = memberRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Auction auction = AuctionMapper.convertAuctionDtoToEntity(dto, seller);

        // 메이저에서 임시 시작 종료시간 넣기
        if (auction.getAuctionType() == AuctionType.MAJOR) {
            auction.setStartTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
            auction.setEndTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));

        }

        if (auction.getAuctionType() == AuctionType.MAJOR) {
            // 경매생성시 자동으로 승인대기 상태로 설정 (DB에서 넣어줘도 될 듯함)
            auction.setAuctionStatus(AuctionStatus.PENDING);
        } else if (auction.getAuctionType() == AuctionType.MINOR) {
            // 경매생성시 자동으로 경매대기 상태로 설정 (DB에서 넣어줘도 될 듯함)
            auction.setAuctionStatus(AuctionStatus.READY);
        }
        // DB에 넣어서 ID 자동 채우기
        Auction savedAuction = auctionRepository.save(auction);

        if (savedAuction.getAuctionType() == AuctionType.MINOR) {
            try {
                // 시작 종료 타이머 걸기
                auctionSchedulerService.scheduleAuctionStart(savedAuction);
                auctionSchedulerService.scheduleAuctionEnd(savedAuction);
            } catch (SchedulerException e) {
                log.error("타이머 지정 실패 : {}", e.getMessage());
                auctionRepository.deleteById(savedAuction.getId());
                return false;
            }
        }
        return true;
    }

    // READ (전체 조회)
    public List<AuctionResDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionMapper::convertEntityToAuctionDto)
                        .collect(Collectors.toList());
    }

    // READ (단건 조회)
    public Auction getAuctionById(Long id) {
        // 캐시에서 먼저 찾고, 없으면 DB에서 조회 후 캐시에 넣어줌
        Auction auction = cache.get(id);
        if (auction == null) {
            try {
                auction = auctionRepository.findById(id).orElseThrow(() -> new RuntimeException("Auction not found"));
            } catch (RuntimeException e){
                log.info("auction 조회 오류 : {}", e.getMessage());
                return null;
            }
            cache.put(id, auction);
        }
        return auction;
    }

    public AuctionResDto getAuctionDtoById(Long id) {
        Auction auction = getAuctionById(id);
        if (auction != null) return AuctionMapper.convertEntityToAuctionDto(auction);
        else return null;
    }

    // cache UPDATE
    public Boolean updateAuction(Auction auction) {
        cache.put(auction.getId(), auction);
        return true;
    }

    // DB UPDATE
    public Auction endAuction(Long id) {
        Auction auction = cache.get(id);
        return auctionRepository.save(auction);
    }

    // DELETE
    public void deleteAuction(Long id) {
        auctionRepository.deleteById(id);
    }


    // 마이페이지 판매 목록 리스트 조회(페이지네이션)
    public Page<AuctionResDto> ListSellAuction(Long id, Pageable pageable) {
        Page<Auction> auction = auctionRepository.findBySellerId(id, pageable);
        return auction.map(AuctionMapper::convertEntityToAuctionDto);
    }
    // 마이페이지 낙찰 내역 리스트 조회(페이지네이션)
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
        if (startTime.isAfter(endTime)) {
            throw new IllegalStateException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
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
        Page<Auction> auction = auctionRepository.findByAuctionType("일반", pageable);
        return auction.map(AuctionMapper::convertEntityToAuctionDto);
    }

    // 프리미엄 경매 리스트 날짜별로 가져오기
    public List<AuctionResDto> getMajorList(LocalDateTime start, LocalDateTime end) {
        List<Auction> auction = auctionRepository.findByAuctionTypeAndStartTimeBetween("프리미엄", start, end);
        return auction.stream().map(AuctionMapper::convertEntityToAuctionDto).toList();
    }
}