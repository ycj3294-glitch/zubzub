package com.example.zubzub.security;

import com.example.zubzub.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final int credit;
    private final String profileImg;

    private final boolean isAdmin;
    private final String memberStatus;

    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.password = member.getPwd();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.credit = member.getCredit();
        this.profileImg = member.getProfileImg();
        this.isAdmin = member.isAdmin();
        this.memberStatus = member.getMemberStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (this.isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // 🚫 탈퇴/정지 회원 로그인 차단
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(this.memberStatus);
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
