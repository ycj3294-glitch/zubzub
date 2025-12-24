package com.example.zubzub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "members")
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
    private String name = "";

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

    @Column(nullable = false)
    private int lockedCredit = 0;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false; // 소문자 i

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

    // 입찰 시 크레딧 잠금
    public void lockCredit(int amount) {
        if (credit - lockedCredit < amount) {
            throw new IllegalArgumentException("크레딧 부족");
        }
        lockedCredit += amount;
    }

    // 입찰 취소 / 상위입찰 발생 시 환불
    public void unlockCredit(int amount) {
        if (lockedCredit < amount) {
            throw new IllegalArgumentException("잠금 크레딧 부족");
        }
        lockedCredit -= amount;
    }

    // 실제 사용 후 차감 (예: 낙찰 시)
    public void useLockedCredit(int amount) {
        if (lockedCredit < amount) {
            throw new IllegalArgumentException("잠금 크레딧 부족");
        }
        credit -= amount;
        lockedCredit -= amount;
    }

    // 남은 사용 가능한 크레딧 조회
    public int getAvailableCredit() {
        return credit - lockedCredit;
    }
}
