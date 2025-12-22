package com.example.zubzub.dto;

import com.example.zubzub.entity.AuctionType;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionCreateDto {

    private AuctionType auctionType;

    private String category;

    private Long sellerId;

    private String itemName;

    private String itemDesc;

    private int startPrice;

    private int minBidUnit;

    private String itemImg;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
