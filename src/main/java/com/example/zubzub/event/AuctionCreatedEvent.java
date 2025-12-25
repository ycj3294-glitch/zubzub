package com.example.zubzub.event;

import com.example.zubzub.entity.Auction;

public class AuctionCreatedEvent {
    private final Auction auction;

    public AuctionCreatedEvent(Auction auction) {
        this.auction = auction;
    }

    public Auction getAuction() {
        return auction;
    }
}

