package com.example.zubzub.dto;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageCreateDto {

    private Long receiverId;

    private String title;

    private String content;

}
