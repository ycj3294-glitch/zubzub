package com.example.zubzub.service;

import com.example.zubzub.component.Broadcaster;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.*;
import com.example.zubzub.mapper.BidHistoryMapper;
import com.example.zubzub.repository.MemberRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

// 실제 경매 진행에 사용되는 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionBidService {
    private final AuctionService auctionService;
    private final AuctionSchedulerService auctionSchedulerService;
    private final BidHistoryService bidHistoryService;
    private final MemberRepository memberRepository;
    private final Broadcaster broadcaster;

    // 입찰 요청을 받았을 때 실행할 로직
    public synchronized boolean placeBid(Long auctionId, BidHistoryCreateDto dto) {

        // 입찰 대상 경매 불러오기
        Auction auction = auctionService.getAuctionEntity(auctionId);
        Member bidder = memberRepository.findById(dto.getBidderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        BidHistory bidHistory = BidHistoryMapper.convertBidHistoryDtoToEntity(dto, auction, bidder);
        bidHistory.setBidTime(LocalDateTime.now());

        // 입찰 가능 여부 확인 (경매 상태)
        if (auction.getAuctionStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("현재 경매는 입찰이 불가능한 상태입니다.");
        }

        // 입찰 가능 여부 확인(크레딧)
        if (bidder.getAvailableCredit() < dto.getPrice()) {
            throw new IllegalArgumentException("크레딧이 부족합니다.");
        }

        // 메이저 경매의 경우
        if (auction.getAuctionType() == AuctionType.MAJOR) {

            // 입찰가가 기존입찰가보다 높지 않으면 입찰 false
            if (bidHistory.getPrice() <= auction.getFinalPrice()) return false;

            // 입찰가가 최소상위입찰가보다 낮으면 입찰 false
            if (bidHistory.getPrice() < auction.getFinalPrice() + auction.getMinBidUnit()) return false;

            // 연장 종료 시간이 비어있으면 기본 종료시간으로 채워주기
            if (auction.getExtendedEndTime() == null) {
                auction.setExtendedEndTime(auction.getEndTime());
            }

            // 입찰후 남은시간이 1분미만이라면 연장종료시간을 입찰시간 + 1분으로 갱신
            if (Duration.between(bidHistory.getBidTime(), auction.getExtendedEndTime()).toMinutes() < 1) {
                auction.setExtendedEndTime(bidHistory.getBidTime().plusMinutes(1));
                try {
                    auctionSchedulerService.rescheduleAuctionEnd(auction);
                    log.info("타이머 재지정 : {}", auction);

                } catch (SchedulerException e) {
                    log.error("타이머 지정 실패 : {}", e.getMessage());
                    return false;
                }
            }

            // 이전 최고 입찰자 환불
            Member prevBidder = auction.getWinner();
            int prevPrice = auction.getFinalPrice() != 0 ? auction.getFinalPrice() : 0;

            if (prevBidder != null && !prevBidder.equals(bidder)) {
                prevBidder.unlockCredit(prevPrice);
            }

            // 현재 입찰자 크레딧 잠금
            bidder.lockCredit(dto.getPrice());

            // 입찰금액, 입찰자 지정
            auction.setFinalPrice(bidHistory.getPrice());
            auction.setWinner(bidHistory.getBidder());

            // 캐시에 경매 정보 업데이트
            auctionService.updateAuction(auction);
            log.info("Updated cache for auction {} with bid {}", auction.getId(), bidHistory);

            // 브로드캐스트
            broadcaster.broadcastAuction(auction);
            log.info("Broadcasted auction {}", auction.getId());
        }

        // 마이너 경매의 경우
        else if (auction.getAuctionType() == AuctionType.MINOR) {
            if (bidHistory.getPrice() > auction.getFinalPrice()) {

                // 현재 입찰자 크레딧 잠금
                bidder.lockCredit(dto.getPrice());

                // 입찰금액, 입찰자 지정
                auction.setFinalPrice(bidHistory.getPrice());
                auction.setWinner(bidHistory.getBidder());

                // 캐시에 경매 정보 업데이트
                auctionService.updateAuction(auction);
                log.info("Updated cache for auction {} with bid {}", auction.getId(), bidHistory);
            }
        }

        // 입찰기록만 비동기 DB 저장
        bidHistoryService.saveBidHistoryAsync(bidHistory);

        return true;
    }
}