package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionResDto {
    private int currentBid;
    private LocalDateTime endDate;
}
