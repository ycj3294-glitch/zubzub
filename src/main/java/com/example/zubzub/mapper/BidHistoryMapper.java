package com.example.zubzub.mapper;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.dto.BidHistoryResDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;

public class BidHistoryMapper {
    public static BidHistory convertBidHistoryDtoToEntity(BidHistoryCreateDto dto) {
        return BidHistory.builder()
                .auctionId((dto.getAuctionId()))
                .memberId(dto.getMemberId())
                .price(dto.getPrice())
                .build();
    }

    public static BidHistoryResDto convertEntityToBidHistoryDto(BidHistory bidHistory) {
        return BidHistoryResDto.builder()
                .id(bidHistory.getId())
                .auctionId(bidHistory.getAuctionId())
                .memberId(bidHistory.getMemberId())
                .price(bidHistory.getPrice())
                .bidTime(bidHistory.getBidTime())
                .build();
    }
}
