package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionCreateDto {

    private String auctionType;

    private String category;

    private Long sellerId;

    private String itemName;

    private String itemDesc;

    private int startPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
