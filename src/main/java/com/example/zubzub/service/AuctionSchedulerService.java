package com.example.zubzub.service;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.job.AuctionEndJob;
import com.example.zubzub.job.AuctionStartJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private final Scheduler scheduler;

    public void scheduleAuctionStart(Auction auction) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(AuctionStartJob.class)
                .withIdentity("auctionStartJob-" + auction.getId(), "auctionGroup")
                .usingJobData("auctionId", auction.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionStartTrigger-" + auction.getId(), "auctionGroup")
                .startAt(java.sql.Timestamp.valueOf(auction.getStartTime()
                ))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void scheduleAuctionEnd(Auction auction) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(AuctionEndJob.class)
                .withIdentity("auctionEndJob-" + auction.getId(), "auctionGroup")
                .usingJobData("auctionId", auction.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionEndTrigger-" + auction.getId(), "auctionGroup")
                .startAt(java.sql.Timestamp.valueOf(auction.getEndTime()))
                .build();
        log.info(jobDetail.getKey().getName());
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void rescheduleAuctionEnd(Auction auction) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey("auctionEndJob-" + auction.getId(), "auctionGroup");

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionEndTrigger-" + auction.getId(), "auctionGroup")
                .startAt(java.sql.Timestamp.valueOf(auction.getExtendedEndTime()))
                .build();

        log.info(triggerKey.getName());
        scheduler.rescheduleJob(triggerKey, trigger);
    }
}
