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
public class Item {
    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sellerId;
    @Column(nullable = false, length = 50)
    private String itemName;
    private String itemDesc;
    private Long startPrice;
    private Long finalPrice;
    private String itemImg;
    private String itemStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
