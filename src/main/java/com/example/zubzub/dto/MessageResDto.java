package com.example.zubzub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageResDto {

    private Long id;

    private String title;

    private String content;

    private boolean isRead;

    private LocalDateTime createdAt;
}
