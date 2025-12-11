package com.example.zubzub.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberResDto {
    private Long id;
    private String email;
    private String pwd;
    private String nickname;
    private String grade;
    private LocalDateTime regDate;
    private int point;
    private String profileImg;
}
