package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentBidResponseDto  {
    private int bidPrice;          // 현재 입찰가
    private LocalDateTime bidTime; // 입찰 시각
}
