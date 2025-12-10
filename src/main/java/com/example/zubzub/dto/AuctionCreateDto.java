package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionCreateDto {
    private String itemName;
    private int startingPrice;
    private LocalDateTime endDate;
}
