package com.example.zubzub.repository;

import com.example.zubzub.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Member findByEmail(String email);
}
