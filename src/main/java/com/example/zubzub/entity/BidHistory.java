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
@Table
public class BidHistory {
    @Id
    @Column(name="bid_history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private Member bidder;

    @Min(1)
    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private LocalDateTime bidTime;
}
