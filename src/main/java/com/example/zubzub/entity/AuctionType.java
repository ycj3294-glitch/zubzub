package com.example.zubzub.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionType {
    MINOR("마이너"),
    MAJOR("메이저");

    private final String label;
}
