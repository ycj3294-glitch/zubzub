package com.example.zubzub.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberSignupReqDto {

    private String email;
    private String pwd;
    private String name;
    private String nickname;
}
