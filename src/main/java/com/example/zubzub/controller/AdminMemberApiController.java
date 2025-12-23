package com.example.zubzub.controller;

import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.dto.AuctionTimeUpdateRequest;
import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.entity.AuctionStatus;
import com.example.zubzub.service.AuctionService;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberApiController {

    private final MemberService memberService;
    private final AuctionService auctionService;

    /**
     * 전체 회원 조회 (관리자용)
     */
    @GetMapping
    public ResponseEntity<List<MemberResDto>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAll());
    }

    /**
     * 회원 일시정지
     * member_status = SUSPENDED
     */
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<?> suspendMember(@PathVariable Long id) {
        memberService.updateStatus(id, false);
        return ResponseEntity.ok("회원이 일시정지되었습니다.");
    }

    /**
     * 회원 활성화
     * member_status = ACTIVE
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateMember(@PathVariable Long id) {
        memberService.updateStatus(id, true);
        return ResponseEntity.ok("회원이 활성화되었습니다.");
    }

    /**
     * 회원 삭제 (Soft Delete)
     * member_status = DELETE_REQ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok("회원이 삭제되었습니다.");
    }

    // 아래로 jwt 관련 없는 작업


    // 프리미엄 경매 일정 배정
    @PostMapping("/approve")
    public ResponseEntity<String> setTime(@RequestBody AuctionTimeUpdateRequest req) {
        Long auctionId = req.getId();
        LocalDateTime start = req.getStartTime();
        LocalDateTime end = start.plusHours(2);
        if(!end.toLocalDate().isEqual(start.toLocalDate())) {
            end =start.toLocalDate().atTime(23, 59);
        }
        auctionService.approveAuction(auctionId);
        auctionService.setTime(auctionId, start, end);
        return ResponseEntity.ok("승인되고 시간이 배정되었습니다.");
    }

    // 대규모 경매 승인대기 목록 가져오기
    @GetMapping("/pending")
    public ResponseEntity<List<AuctionResDto>> getPendingList() {
        log.info("승인 대기 목록 요청받음");
        return ResponseEntity.ok(auctionService.getPendingList());
    }
}