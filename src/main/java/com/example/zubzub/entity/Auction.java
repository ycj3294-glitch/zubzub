package com.example.zubzub.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "auctions")
public class Auction {
    @Id
    @Column(name="Auction_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)   // DB에 문자열로 저장
    @Column(nullable = false)
    private AuctionType auctionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="seller_Id", nullable = false)
    private Member seller;

    @Column(nullable = false)
    private String itemDesc;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 50)
    private String itemName;

    @Column
    private String itemImg;

    @Min(1)
    @Column(nullable = false)
    private int startPrice;

    @Min(1)
    @Column(nullable = false)
    private int minBidUnit;

    @Column(nullable = false)
    private int bidCount;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)   // DB에 문자열로 저장
    @Column(nullable = false)
    private AuctionStatus auctionStatus;

    @Column
    private int finalPrice;

    @Column
    private LocalDateTime extendedEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="winner_id")
    private Member winner;
}
