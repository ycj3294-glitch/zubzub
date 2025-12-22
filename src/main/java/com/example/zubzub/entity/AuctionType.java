package com.example.zubzub.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionType {
    MINOR("메이저"),
    MAJOR("마이너");

    private final String label;
}
