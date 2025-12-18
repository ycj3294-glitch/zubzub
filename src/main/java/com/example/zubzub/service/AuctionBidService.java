package com.example.zubzub.service;

import com.example.zubzub.component.Broadcaster;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.mapper.BidHistoryMapper;
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
    private final Broadcaster broadcaster;

    // 입찰 요청을 받았을 때 실행할 로직
    public synchronized boolean placeBid(Long auctionId, BidHistoryCreateDto dto) {

        // 입찰 대상 경매 불러오기
        BidHistory bidHistory = BidHistoryMapper.convertBidHistoryDtoToEntity(dto);
        bidHistory.setAuctionId(auctionId);
        bidHistory.setBidTime(LocalDateTime.now());
        Auction auction = auctionService.getAuctionById(auctionId);

        // 경매중 상태가 아니면 입찰 false
        if (auction.getAuctionStatus() != AuctionStatus.ACTIVE) return false;

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

        // 입찰자, 입찰금액 지정
        auction.setWinnerId(bidHistory.getMemberId());
        auction.setFinalPrice(bidHistory.getPrice());

        // 캐시에 경매 정보 업데이트
        auctionService.updateAuction(auction);
        log.info("Updated cache for auction {} with bid {}", auction.getId(), bidHistory);

        // 브로드캐스트
        broadcaster.broadcastAuction(auction);
        log.info("Broadcasted auction {}", auction.getId());

        // 입찰기록만 비동기 DB 저장
        bidHistoryService.saveBidHistoryAsync(bidHistory);

        return true;
    }
}