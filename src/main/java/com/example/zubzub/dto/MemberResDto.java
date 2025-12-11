package com.example.zubzub.dto;


import com.example.zubzub.entity.Member;
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
    private int credit;
    private String profileImg;


    public MemberResDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.pwd = member.getPwd();
        this.nickname = member.getNickname();
        this.grade = member.getGrade();
        this.regDate = member.getRegDate();
        this.credit = member.getCredit();  // or credit
        this.profileImg = member.getProfileImg();
    }
}

