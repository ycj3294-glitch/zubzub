package com.example.zubzub.mapper;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
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

    public static AuctionResDto convertEntityToAuctionDto(Auction auction) {
        return AuctionResDto.builder()
                .id(auction.getId())
                .auctionStatus(auction.getAuctionStatus().getLabel()) // Enum이면 toString() 또는 getLabel()
                .auctionType(auction.getAuctionType())
                .category(auction.getCategory())
                .sellerId(auction.getSellerId())
                .itemName(auction.getItemName())
                .itemDesc(auction.getItemDesc())
                .startPrice(auction.getStartPrice())
                .finalPrice(auction.getFinalPrice())
                .itemImg(auction.getItemImg())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .extendedEndTime(auction.getExtendedEndTime())
                .winnerId(auction.getWinnerId())
                .build();
    }
}
