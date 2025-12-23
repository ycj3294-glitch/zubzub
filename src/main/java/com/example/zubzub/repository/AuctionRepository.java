package com.example.zubzub.repository;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.entity.AuctionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 판매자 id로 auction 내역 조회(페이지네이션)
    Page<Auction> findBySellerId(Long id, Pageable pageable);
    // 낙찰자 id로 auction 내역 조회(페이지네이션)
    Page<Auction> findByWinnerId(Long id, Pageable pageable);
    // AuctionType으로 조회(페이지네이션)
    Page<Auction> findByAuctionType(AuctionType type, Pageable pageable);


    // AuctionType과 날짜로 조회
    List<Auction> findByAuctionTypeAndStartTimeBetween(AuctionType type, LocalDateTime start, LocalDateTime end);

    // AuctionStatus로 조회
    List<Auction> findByAuctionStatus(AuctionStatus status);
}
