package com.example.zubzub.dto;

import lombok.*;


@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidHistoryCreateDto {
    private Long auctionId;
    private Long bidderId;
    private int price;
}