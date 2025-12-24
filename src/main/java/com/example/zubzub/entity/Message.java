package com.example.zubzub.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "messages")
public class Message {
    @Id
    @Column(name="Message_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    // 제목
    @Column(length = 255)
    private String title;

    // 본문
    @Column(nullable = false)
    private String content;

    // 읽음 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // 보낸 시각
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 삭제 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
