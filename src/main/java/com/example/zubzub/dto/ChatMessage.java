package com.example.zubzub.dto;

import lombok.*;

import java.awt.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String sender;
    private String content;
}
