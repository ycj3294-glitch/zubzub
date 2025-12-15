package com.example.zubzub.controller;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.service.AuctionBidService;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final AuctionBidService auctionBidService;

    // 경매 하나 보기
    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    // 경매 생성
    @PostMapping()
    public ResponseEntity<Boolean> createAuction(@RequestBody AuctionCreateDto dto) {
        return ResponseEntity.ok(auctionService.createAuction(dto));
    }

    // 경매에 입찰하기
    @PostMapping("/{id}/bids")
    public ResponseEntity<Boolean> placeBid(@PathVariable Long id, @RequestBody BidHistoryCreateDto dto) {
        return ResponseEntity.ok(auctionBidService.placeBid(id, dto));
    }
}
