package com.example.zubzub.controller;

import com.example.zubzub.dto.AuctionCreateDto;
import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.Auction;
import com.example.zubzub.service.AuctionBidService;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final AuctionBidService auctionBidService;

    // 경매 하나 보기
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResDto> getAuction(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionDtoById(id));
    }

    // 경매 생성
    @PostMapping()
    public ResponseEntity<Void> createAuction(@RequestBody AuctionCreateDto dto) {
        auctionService.createAuction(dto);
        System.out.println("DF");
        return ResponseEntity.ok().build();
    }

    // 경매에 입찰하기
    @PostMapping("/{id}/bids")
    public ResponseEntity<Void> placeBid(@PathVariable Long id, @RequestBody BidHistoryCreateDto dto) {
        auctionBidService.placeBid(id, dto);
        return ResponseEntity.ok().build();
    }

    // 판매 내역 가져오기
    @GetMapping("/{id}/selllist")
    public ResponseEntity<Page<AuctionResDto>> getSellList(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auctionService.ListSellAuction(id, pageable));
    }
    // 낙찰 내역 가져오기
    @GetMapping("/{id}/winlist")
    public ResponseEntity<Page<AuctionResDto>> getWinnerList(@PathVariable Long id,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auctionService.ListWinnerAuction(id, pageable));
    }
}
