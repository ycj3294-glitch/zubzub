package com.example.zubzub.mapper;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;

public class AuctionMapper {
    public static BidHistory convertBidHistoryDtoToEntity(BidHistoryCreateDto dto) {
        return BidHistory.builder()
                .auctionId((dto.getAuctionId()))
                .memberId(dto.getMemberId())
                .price(dto.getPrice())
                .build();
    }

    public static Auction convertAuctionDtoToEntity(AuctionCreateDto dto) {
        return Auction.builder()
                .auctionType(dto.getAuctionType())
                .category(dto.getCategory())
                .sellerId(dto.getSellerId())
                .itemName(dto.getItemName())
                .itemDesc(dto.getItemDesc())
                .startPrice(dto.getStartPrice())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
