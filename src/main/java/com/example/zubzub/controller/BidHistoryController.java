package com.example.zubzub.controller;

import com.example.zubzub.dto.BidHistoryResDto;
import com.example.zubzub.service.AuctionBidService;
import com.example.zubzub.service.BidHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bid-histories")
@RequiredArgsConstructor
public class BidHistoryController {

    private final BidHistoryService bidHistoryService;
    private final AuctionBidService auctionBidService;

    @GetMapping
    public ResponseEntity<List<BidHistoryResDto>> list(
            @RequestParam Long auctionId,
            @PageableDefault(page = 0, size = 20, sort = "bidTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bidHistoryService.findByAuctionId(auctionId, pageable));
    }

    //가장 최근 입찰 기록 가져오기
    @GetMapping("/{auctionid}/lastbid")
    public ResponseEntity<BidHistoryResDto> lastBid(@PathVariable("auctionid") Long auctionId, @RequestParam Long bidderId) {
        return ResponseEntity.ok(auctionBidService.getPresentBidHistory(auctionId, bidderId));
    }
}
