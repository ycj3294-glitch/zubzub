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
    @Column(name="auction_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemName;

    @Column(nullable = false)
    private int startingPrice;

    @Column(nullable = false)
    private LocalDateTime regDate;

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }

    @Column(nullable = false)
    private LocalDateTime endDate;
}
