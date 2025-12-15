package com.example.zubzub.job;

import com.example.zubzub.component.AuctionBroadcaster;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionStartJob implements Job {

    private final AuctionService auctionService;
    private final AuctionBroadcaster auctionBroadcaster;

    @Override
    public void execute(JobExecutionContext context) {
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        Auction auction = auctionService.getAuctionById(auctionId);
        auction.setItemStatus("경매중");
        auctionService.updateAuction(auctionId, auction);
        auctionBroadcaster.broadcast(auction);
        System.out.println("Auction " + auctionId + " 시작 처리 실행!");
    }
}