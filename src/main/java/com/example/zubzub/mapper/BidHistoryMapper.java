package com.example.zubzub.mapper;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.dto.BidHistoryResDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.entity.Member;

public class BidHistoryMapper {
    public static BidHistory convertBidHistoryDtoToEntity(BidHistoryCreateDto dto, Auction auction, Member bidder) {
        return BidHistory.builder()
                .auction(auction)
                .bidder(bidder)
                .price(dto.getPrice())
                .build();
    }

    public static BidHistoryResDto convertEntityToBidHistoryDto(BidHistory bidHistory) {
        return BidHistoryResDto.builder()
                .id(bidHistory.getId())
                .auctionId(bidHistory.getAuction().getId())
                .bidderId(bidHistory.getBidder().getId())
                .price(bidHistory.getPrice())
                .bidTime(bidHistory.getBidTime())
                .build();
    }
}
