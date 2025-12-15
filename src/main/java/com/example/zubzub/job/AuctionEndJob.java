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
public class AuctionEndJob implements Job {

    private final AuctionService auctionService;
    private final AuctionBroadcaster auctionBroadcaster;

    // 경매종료시 실행할 로직
    @Override
    public void execute(JobExecutionContext context) {

        // 경매 불러오기
        Long auctionId = context.getJobDetail().getJobDataMap().getLong("auctionId");
        Auction auction = auctionService.getAuctionById(auctionId);

        // 경매종료 상태로 설정
        auction.setItemStatus("경매종료");

        // 캐시에 업데이트
        auctionService.updateAuction(auctionId, auction);

        // 브로드캐스트
        auctionBroadcaster.broadcast(auction);

        // DB에 업데이트
        auctionService.endAuction(auctionId);

        System.out.println("Auction " + auctionId + " 종료 처리 실행!");
    }
}