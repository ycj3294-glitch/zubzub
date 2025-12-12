package com.example.zubzub.controller;

import com.example.zubzub.entity.Auction;
import com.example.zubzub.repository.AuctionRepository;
import com.example.zubzub.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    // 회원 id에 따른 판매내역 반환
    @GetMapping("/selllist")
    public ResponseEntity<List<Auction>> sellList(Long id) {
        return ResponseEntity.ok(auctionService.sellList(id));
    }
    // 회원 id에 따른 낙찰내역 반환
    @GetMapping("/winnerlist")
    public ResponseEntity<List<Auction>> winnerList(Long id) {
        return ResponseEntity.ok(auctionService.winnerList(id));
    }
}
