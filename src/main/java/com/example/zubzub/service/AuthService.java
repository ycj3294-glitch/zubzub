package com.example.zubzub.service;

import com.example.zubzub.dto.LoginReqDto;
import com.example.zubzub.dto.SignUpReqDto;
import com.example.zubzub.entity.Member;
import com.example.zubzub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service    // Spring Container에 Bean등록, 싱글톤 객체가 됨
@RequiredArgsConstructor    // 매개변수가 전부 있는 생성자를 자동 생성
@Transactional  // 작업 중 한개라도 실패하면 자동 롤백
public class AuthService {
    private final MemberRepository memberRepository;    // 생성자를 통한 의존성 주입

    // 회원 가입 여부 확인
    public boolean isMember(String email) {
        return memberRepository.existsByEmail(email);
    }
    // 회원 가입
    public boolean signUp(SignUpReqDto dto) {
        try {
            Member member = convertDtoToEntity(dto);
            memberRepository.save(member);  // save는 내부적으로 insert와 update 역할을 함
            return true;
        } catch (Exception e) {
            log.error("회원 가입 시 DB 오류 발생 : {}", e.getMessage());
            return false;
        }
    }

    // 로그인
    public boolean login(LoginReqDto dto) {
        Optional<Member> member = memberRepository
                .findByEmailAndPwd(dto.getEmail(), dto.getPwd());
        return member.isPresent();
    }

    // DTO -> Entity로 변환하는 메서드
    /*private Member convertDtoToEntity(SignUpReqDto dto) {
        Member member = new Member();
        member.setEmail(dto.getEmail());
        member.setPwd(dto.getPwd());
        member.setName(dto.getName());
        return member;
    }*/
    // DTO -> Entity 변환 (빌더 패턴 적용)
    private Member convertDtoToEntity(SignUpReqDto dto) {
        return Member.builder()
                .email(dto.getEmail())
                .pwd(dto.getPwd())
                .name(dto.getName())
                .build();
    }
}
