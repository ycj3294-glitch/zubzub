package com.example.zubzub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    /* =========================
       로그인 / 식별
       ========================= */

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String pwd;

    /* =========================
       기본 회원 정보 (회원가입 필수)
       ========================= */

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    /* =========================
       선택 정보 (회원가입 이후)
       ========================= */

    @Column(name = "profile_img")
    private String profileImg = "";

    @Column(nullable = false)
    private String addr = ""; // 기본값 빈 문자열

    /* =========================
       시스템 필드
       ========================= */

    @Column(nullable = false)
    private int credit = 0;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    /**
     * ACTIVE      : 정상
     * SUSPENDED   : 관리자 정지
     * DELETE_REQ  : 탈퇴 요청
     */
    @Column(name = "member_status", nullable = false)
    private String memberStatus = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
