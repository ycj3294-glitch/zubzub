package com.example.zubzub.repository;

import com.example.zubzub.entity.Member;
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
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원 조회 - 성공")
    void findByEmail_success() {
        // given
        Member member = new Member();
        member.setEmail("test@test.com");
        member.setPwd("1234");
        member.setNickname("tester");

        memberRepository.save(member);

        // when
        Member result = memberRepository.findByEmail("test@test.com");

        // then
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName("이메일 중복 확인")
    void existsByEmail() {
        // given
        Member member = new Member();
        member.setEmail("dup@test.com");
        member.setPwd("1234");
        member.setNickname("dup");

        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail("dup@test.com");

        // then
        assertTrue(exists);
    }
}