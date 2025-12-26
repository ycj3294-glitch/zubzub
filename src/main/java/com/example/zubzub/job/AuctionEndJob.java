package com.example.zubzub.job;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionType;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEndJob implements Job {

    private final AuctionService auctionService;

    // 경매종료시 실행할 로직
    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        Auction auction = auctionService.getAuctionEntity(auctionId);

        if (auction.getAuctionType() == AuctionType.MAJOR) {
            auctionService.endAuction(auctionId);
        } else if (auction.getAuctionType() == AuctionType.MINOR) {
            auctionService.endMinorAuction(auctionId);
        }
    }
}