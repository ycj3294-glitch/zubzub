package com.example.zubzub.repository;

import com.example.zubzub.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 판매자 id로 auction 내역 조회(페이지네이션)
    Page<Auction> findBySellerId(Long id, Pageable pageable);
    // 낙찰자 id로 auction 내역 조회(페이지네이션)
    Page<Auction> findByWinnerId(Long id, Pageable pageable);
}
