package com.example.zubzub.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateReqDto {

    private String nickname;
    private String name;
    private String pwd;
    private String addr;
    private String profileImg;
}