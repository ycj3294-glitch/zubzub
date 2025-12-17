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
    private String name;
    private String nickname;
    private String profileImg;
    private int credit;
    private boolean isAdmin;
    private String memberStatus;
    private LocalDateTime createdAt;

    public MemberResDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.profileImg = member.getProfileImg();
        this.credit = member.getCredit();
        this.isAdmin = member.isAdmin();
        this.memberStatus = member.getMemberStatus();
        this.createdAt = member.getCreatedAt();
    }
}

