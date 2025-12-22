package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidHistoryResDto {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private int price;
    private LocalDateTime bidTime;
}