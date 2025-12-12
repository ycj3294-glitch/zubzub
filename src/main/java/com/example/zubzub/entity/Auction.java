package com.example.zubzub.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@Entity
@Builder
@Table
public class Auction {
    @Id
    @Column(name="Auction_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String auctionType;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 50)
    private String itemName;

    @Column(nullable = false)
    private String itemDesc;

    @Column(nullable = false)
    private int startPrice;

    @Column
    private int finalPrice;

    @Column
    private String itemImg;

    @Column(nullable = false)
    private String itemStatus;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private LocalDateTime extendedEndTime;

    @Column
    private Long winnerId;
}
