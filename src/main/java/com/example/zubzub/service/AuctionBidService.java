package com.example.zubzub.service;

import com.example.zubzub.component.AuctionBroadcaster;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.example.zubzub.mapper.AuctionMapper.convertBidHistoryDtoToEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionBidService {
    private final AuctionService auctionService;
    private final AuctionSchedulerService auctionSchedulerService;
    private final BidHistoryService bidHistoryService;
    private final AuctionBroadcaster auctionBroadcaster;

    public synchronized boolean placeBid(Long auctionId, BidHistoryCreateDto dto) {
        BidHistory bidHistory = convertBidHistoryDtoToEntity(dto);
        bidHistory.setAuctionId(auctionId);
        bidHistory.setBidTime(LocalDateTime.now());

        // 1. 상태 업데이트
        Auction auction = auctionService.getAuctionById(auctionId);
        // 경매중 상태가 아니면 false
        if (!auction.getItemStatus().equals("경매중")) return false;
        // 연장 종료 시간이 비어있으면 채워주기
        if (auction.getExtendedEndTime() == null) {
            auction.setExtendedEndTime(auction.getEndTime());
        }
        // 입찰후 남은시간이 1분미만이라면 연장종료시간을 입찰시간 + 1분으로 갱신
        if (Duration.between(bidHistory.getBidTime(), auction.getExtendedEndTime()).toMinutes() < 1) {
            auction.setExtendedEndTime(bidHistory.getBidTime().plusMinutes(1));
            try {
                auctionSchedulerService.rescheduleAuctionEnd(auction);
            } catch (SchedulerException e) {
                log.error("타이머 지정 실패 : {}", e.getMessage());
                return false;
            }
        }
        auction.setWinnerId(bidHistory.getMemberId());
        auction.setFinalPrice(bidHistory.getPrice());
        auctionService.updateAuction(auctionId, auction);
        log.info("Updated cache for auction {} with bid {}", auctionId, bidHistory);

        // 2. 브로드캐스트
        auctionBroadcaster.broadcast(auction);
        log.info("Broadcasted auction {}", auctionId);

        // 3. 비동기 DB 저장
        bidHistoryService.saveBidHistoryAsync(bidHistory);

        return true;
    }
}