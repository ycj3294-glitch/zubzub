package com.example.zubzub.service;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.job.AuctionEndJob;
import com.example.zubzub.job.AuctionStartJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private final Scheduler scheduler;

    // auction.getStartTime()으로 경매시작 타이머 설정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleAuctionStart(Auction auction) throws SchedulerException {
        // 경매시작시 사용할 로직 할당
        JobDetail jobDetail = JobBuilder.newJob(AuctionStartJob.class)
                .withIdentity("auctionStartJob-" + auction.getId(), "auctionGroup")
                .usingJobData("auctionId", auction.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionStartTrigger-" + auction.getId(), "auctionGroup")
                .startAt(java.sql.Timestamp.valueOf(auction.getStartTime()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionIgnoreMisfires()) // 시간이 지났으면 즉시 실행
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    // auction.getEndTime()으로 경매종료 타이머 설정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleAuctionEnd(Auction auction) throws SchedulerException {
        // 경매종료시 사용할 로직 할당
        JobDetail jobDetail = JobBuilder.newJob(AuctionEndJob.class)
                .withIdentity("auctionEndJob-" + auction.getId(), "auctionGroup")
                .usingJobData("auctionId", auction.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionEndTrigger-" + auction.getId(), "auctionGroup")
                .startAt(java.sql.Timestamp.valueOf(auction.getEndTime()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionIgnoreMisfires()) // 시간이 지났으면 즉시 실행
                .build();
        log.info(jobDetail.getKey().getName());
        scheduler.scheduleJob(jobDetail, trigger);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void rescheduleAuctionStart(Auction auction) throws SchedulerException {
        // 기존 타이머와 동일한 키 할당
        TriggerKey triggerKey = new TriggerKey("auctionStartTrigger-" + auction.getId(), "auctionGroup");

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(java.sql.Timestamp.valueOf(auction.getStartTime()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionIgnoreMisfires()) // 시간이 지났으면 즉시 실행
                .build();

        log.info(triggerKey.getName());
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // auction.getExtendedEndTime()으로 경매종료 타이머 재설정
    public void rescheduleAuctionEnd(Auction auction) throws SchedulerException {
        // 기존 타이머와 동일한 키 할당
        TriggerKey triggerKey = new TriggerKey("auctionEndTrigger-" + auction.getId(), "auctionGroup");

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(java.sql.Timestamp.valueOf(auction.getExtendedEndTime()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionIgnoreMisfires()) // 시간이 지났으면 즉시 실행
                .build();

        log.info(triggerKey.getName());
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    public boolean isScheduled(Long auctionId) throws SchedulerException {
        return scheduler.checkExists(JobKey.jobKey("auctionStartJob-" + auctionId, "auctionGroup"));
    }
}


