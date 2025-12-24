package com.example.zubzub.repository;

import com.example.zubzub.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 받는사람 id로 message 내역 조회(페이지네이션)
    Page<Message> findByReceiverId(Long id, Pageable pageable);

    Optional<Message> findByIdAndReceiverId(Long id, Long receiverId);

    void deleteByIdAndReceiverId(Long id, Long receiverId);
}
