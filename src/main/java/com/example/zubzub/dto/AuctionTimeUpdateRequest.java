package com.example.zubzub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionTimeUpdateRequest {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
