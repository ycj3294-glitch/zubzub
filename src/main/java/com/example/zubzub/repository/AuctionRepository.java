package com.example.zubzub.repository;


import com.example.zubzub.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 해당 회원의 낙찰 리스트 반환
    List<Auction> findByWinnerId(Long id);
    // 해당 회원인 경매 리스트 반환
    List<Auction> findBySellerId(Long id);
}
