package com.example.zubzub.controller;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.dto.BidHistoryResDto;
import com.example.zubzub.entity.BidHistory;
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
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/api/bid-histories")
@RequiredArgsConstructor
public class BidHistoryController {

    private final BidHistoryService bidHistoryService;

    @GetMapping
    public ResponseEntity<List<BidHistoryResDto>> list(
            @RequestParam Long auctionId,
            @PageableDefault(page = 0, size = 20, sort = "bidTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bidHistoryService.findByAuctionId(auctionId, pageable));
    }

}
