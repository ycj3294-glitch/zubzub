package com.example.zubzub.dto;

import jakarta.persistence.*;
import lombok.*;


@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidHistoryCreateDto {
    private Long memberId;
    private int bidPrice;
}