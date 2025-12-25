package com.example.zubzub.job;

import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionStartJob implements Job {

    private final AuctionService auctionService;

    // 경매시작시 실행할 로직
    @Transactional
    @Override
    public void execute(JobExecutionContext context) {

        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        auctionService.startAuction(auctionId);
    }
}