package com.example.zubzub.dto;

import com.example.zubzub.entity.AuctionStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionResDto {
    private Long id;

    private String auctionType;

    private String category;

    private Long sellerId;

    private String itemName;

    private String itemDesc;

    private int startPrice;

    private int finalPrice;

    private String itemImg;

    private String auctionStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime extendedEndTime;

    private Long winnerId;
}
