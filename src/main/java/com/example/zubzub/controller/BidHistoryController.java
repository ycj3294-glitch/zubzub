package com.example.zubzub.controller;

import com.example.zubzub.dto.BidHistoryCreateDto;
import com.example.zubzub.entity.BidHistory;
import com.example.zubzub.service.BidHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/bid-history")
@RequiredArgsConstructor
public class BidHistoryController {

    private final BidHistoryService bidHistoryService;

//    @PostMapping
//    public BidHistory create(@RequestBody BidHistoryCreateDto dto) {
//        return bidHistoryService.save(dto);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<BidHistory>  get(@PathVariable Long id) {
        return ResponseEntity.ok(bidHistoryService.findById(id));
    }

}
