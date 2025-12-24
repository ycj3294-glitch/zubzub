package com.example.zubzub.service;

import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")

class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입 완료")
    void completeSignup_success() {
        // given
        MemberSignupReqDto dto = new MemberSignupReqDto();
        dto.setEmail("new@test.com");
        dto.setPwd("1234");
        dto.setName("이현수");
        dto.setNickname("newUser");

        // when
        memberService.completeSignup(dto);

        // then
        Member member = memberRepository.findByEmail("new@test.com");
        assertNotNull(member);
        assertEquals("newUser", member.getNickname());
    }

    @Test
    @DisplayName("닉네임 중복 확인")
    void nickname_duplicate_check() {
        // given
        Member member = new Member();
        member.setEmail("a@test.com");
        member.setPwd("1234");
        member.setNickname("중복닉");

        memberRepository.save(member);

        // when
        boolean result = memberService.isNicknameExists("중복닉");

        // then
        assertTrue(result);
    }
}