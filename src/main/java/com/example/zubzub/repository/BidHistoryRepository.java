package com.example.zubzub.repository;

import com.example.zubzub.entity.BidHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {
    Page<BidHistory> findByAuctionId(Long auctionId, Pageable pageable);
    // 일반경매 최고 입찰자 조회
    BidHistory findTopByAuctionIdOrderByPriceDescBidTimeAsc(Long auctionId);
}
