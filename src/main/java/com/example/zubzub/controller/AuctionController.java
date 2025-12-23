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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    // 일반 경매 수정
    @PatchMapping("/{id}/normalupdate")
    public ResponseEntity<String> updateNormalAuction(@PathVariable Long id, @RequestBody AuctionCreateDto req) {
        auctionService.updateNormalAuction(id, req);
        return ResponseEntity.ok("수정이 완료되었습니다.");
    }

    // 소규모 경매 리스트 보여주기
    @GetMapping("/minorlist")
    public ResponseEntity<?> getMinorList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuctionResDto> list = auctionService.getMinorList(pageable);
            System.out.println("Page: " + page + ", Size: " + size);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace(); // 서버 콘솔에서 실제 에러 확인 가능
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 대규모 경매 리스트 날짜별로 보여주기
    @GetMapping("/majorlist/by-date")
    public ResponseEntity<List<AuctionResDto>> getMajorList(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date); // 문자열을 날짜로
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusSeconds(1); // 시작시간 끝시간 지정(하루범위를 지정)
        return ResponseEntity.ok(auctionService.getMajorList(start, end));
    }
    // 대규모 경매 캘린더용 월별로 보여주기
}
