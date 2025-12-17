package com.example.zubzub.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionStatus {
    PENDING("승인대기"),
    READY("경매대기"),
    ACTIVE("경매중"),
    COMPLETED("경매완료");

    private final String label;
}
