package com.example.zubzub.repository;

import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {
    Page<BidHistory> findByAuctionId(Long auctionId, Pageable pageable);

    Optional<BidHistory> findTopByBidderOrderByBidTimeDesc(Member bidder);

    // 일반경매 최고 입찰자 조회
    BidHistory findTopByAuctionIdOrderByPriceDescBidTimeAsc(Long auctionId);
}
