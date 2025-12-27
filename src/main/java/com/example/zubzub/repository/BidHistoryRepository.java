package com.example.zubzub.repository;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {
    Page<BidHistory> findByAuctionId(Long auctionId, Pageable pageable);

    Optional<BidHistory> findTopByBidderAndAuctionOrderByBidTimeDesc(Member bidder, Auction auction);

    // 일반경매 최고 입찰자 조회
    BidHistory findTopByAuctionIdOrderByPriceDescBidTimeAsc(Long auctionId);

    // auctionId + userId로 최근 1건 조회
    Optional<BidHistory> findTopByAuctionIdAndBidderIdOrderByBidTimeDesc(Long auctionId, Long bidderId);
}
