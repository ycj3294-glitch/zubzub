package com.example.zubzub.controller;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/{itemId}")
    public CurrentBidResponseDto getCurrentBid(@PathVariable Long itemId) {
        return auctionService.getCurrentBid(itemId);
    }

    @PostMapping("/{itemId}/bids")
    public void placeBid(@PathVariable Long itemId, @RequestBody BidHistoryCreateDto dto) {
        auctionService.placeBid(itemId, dto);
    }
}
