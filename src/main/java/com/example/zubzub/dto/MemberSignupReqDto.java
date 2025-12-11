package com.example.zubzub.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter@Setter@ToString@NoArgsConstructor
public class MemberSignupReqDto {
    private String grade = "사용자";
    private String email;
    private String pwd;
    private String passwordCheck; // 추가 비밀번호 검증
    private String nickname;  // name → nickname으로 변경
    private String profileImg; // 프로필 이미지 추가

}
