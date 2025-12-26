package com.example.zubzub.dto;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionType;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionResDto {
    private Long id;

    private AuctionType auctionType;

    private String category;

    private Long sellerId;

    private String sellerNickname;

    private String itemName;

    private String itemDesc;

    private int startPrice;

    private int finalPrice;

    private int minBidUnit;

    private int bidCount;

    private String itemImg;

    private String auctionStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime extendedEndTime;

    private Long winnerId;

    private String winnerNickname;

    public static AuctionResDto from(Auction auction) {
        return AuctionResDto.builder()
                .id(auction.getId())
                .auctionType(auction.getAuctionType())
                .category(auction.getCategory()) // ✅ String 그대로
                .sellerId(auction.getSeller().getId())
                .sellerNickname(auction.getSeller().getNickname())
                .itemName(auction.getItemName())
                .itemDesc(auction.getItemDesc())
                .startPrice(auction.getStartPrice())
                .finalPrice(auction.getFinalPrice())
                .minBidUnit(auction.getMinBidUnit())
                .bidCount(auction.getBidCount())
                .itemImg(auction.getItemImg())
                .auctionStatus(auction.getAuctionStatus().name()) // ✅ enum → String
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .extendedEndTime(auction.getExtendedEndTime())
                .winnerId(
                        auction.getWinner() != null
                                ? auction.getWinner().getId()
                                : null
                )
                .winnerNickname(
                        auction.getWinner() != null
                                ? auction.getWinner().getNickname()
                                : null
                )
                .build();
    }



}
