package com.example.zubzub.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Builder
@Table
public class BidHistory {
    @Id
    @Column(name="bid_history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int bidPrice;

    @Column(nullable = false)
    private LocalDateTime bidTime;
}
