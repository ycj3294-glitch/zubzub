package com.example.zubzub.repository;

import com.example.zubzub.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email); // 이메일을 전달 받아 회원 정보를 반환

    boolean existsByEmail(String email);    // 이메일을 전달받아 가입 여부 확인

    Optional<Member> findByEmailAndPwd(String email, String pwd);   // 로그인 성공/실패
}
