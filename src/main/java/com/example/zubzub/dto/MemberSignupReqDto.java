package com.example.zubzub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberSignupReqDto {
    private String email;
    private String pwd;
    private String passwordCheck;
    private String nickname;
    private String profileImg;
    private String grade = "사용자"; // 기본값
}

