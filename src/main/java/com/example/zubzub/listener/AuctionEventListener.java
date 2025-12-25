package com.example.zubzub.listener;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.event.AuctionCreatedEvent;
import com.example.zubzub.service.AuctionSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionEventListener {

    private final AuctionSchedulerService auctionSchedulerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionCreated(AuctionCreatedEvent event) throws SchedulerException {

        log.info("이벤트 리스닝 : {}", event.getAuction());

        Auction auction = event.getAuction();

        // 시작 스케줄러 등록
        try {
            if (auctionSchedulerService.isScheduled(auction.getId())) {
                auctionSchedulerService.rescheduleAuctionStart(auction);
                log.info("경매 {} 시작 타이머 재설정 완료", auction.getId());
            } else {
                auctionSchedulerService.scheduleAuctionStart(auction);
                log.info("경매 {} 시작 타이머 새로 등록 완료", auction.getId());
            }
        } catch (SchedulerException e) {
            log.error("시작 타이머 지정 실패 : {}", e.getMessage());
        }
    }
}
