package com.example.zubzub.mapper;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.Member;

public class AuctionMapper {

    public static Auction convertAuctionDtoToEntity(AuctionCreateDto dto, Member seller) {
        return Auction.builder()
                .auctionType(dto.getAuctionType())
                .category(dto.getCategory())
                .seller(seller)
                .itemName(dto.getItemName())
                .itemDesc(dto.getItemDesc())
                .startPrice(dto.getStartPrice())
                .itemImg(dto.getItemImg())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .minBidUnit(dto.getMinBidUnit())
                .build();
    }

    public static AuctionResDto convertEntityToAuctionDto(Auction auction) {
        return AuctionResDto.builder()
                .id(auction.getId())
                .auctionStatus(auction.getAuctionStatus() != null ? auction.getAuctionStatus().getLabel() : null)
                .auctionType(auction.getAuctionType())
                .category(auction.getCategory())
                .sellerId(auction.getSeller() != null ? auction.getSeller().getId() : null)
                .itemName(auction.getItemName())
                .itemDesc(auction.getItemDesc())
                .startPrice(auction.getStartPrice())
                .finalPrice(auction.getFinalPrice())
                .itemImg(auction.getItemImg())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .minBidUnit(auction.getMinBidUnit())
                .extendedEndTime(auction.getExtendedEndTime())
                .winnerId(auction.getWinner() != null ? auction.getWinner().getId() : null)
                .build();
    }
}
