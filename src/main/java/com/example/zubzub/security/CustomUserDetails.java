package com.example.zubzub.security;

import com.example.zubzub.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security에서 인증된 사용자 정보를 담는 클래스
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;   // pwd
    private final String nickname;
    private final String grade;
    private final LocalDateTime regDate;
    private final int credit;
    private final String profileImg;

    /** Member → CustomUserDetails 로 변환하는 생성자 */
    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.password = member.getPwd();
        this.nickname = member.getNickname();
        this.grade = member.getGrade();
        this.regDate = member.getRegDate();
        this.credit = member.getCredit();
        this.profileImg = member.getProfileImg();
    }

    /** 권한 지정 메서드 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if ("관리자".equals(this.grade)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    /** Spring Security에서 username = email */
    @Override
    public String getUsername() {
        return this.email;
    }

    /** 비밀번호 반환 */
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}
