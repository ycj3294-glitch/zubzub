package com.example.zubzub.controller;

import com.example.zubzub.dto.AuctionResDto;
import com.example.zubzub.dto.AuctionTimeUpdateRequest;
import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.service.AuctionService;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // 프리미엄 경매 승인
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveAuction(@PathVariable Long id) {
        auctionService.approveAuction(id);
        return ResponseEntity.ok("승인되었습니다.");
    }
    // 프리미엄 경매 일정 배정
    @PostMapping("/settime")
    public ResponseEntity<String> setTime(@RequestBody AuctionTimeUpdateRequest req) {
        auctionService.setTime(req.getId(), req.getStartTime(), req.getEndTime());
        return ResponseEntity.ok("시간이 배정되었습니다.");
    }

    // 대규모 경매 승인대기 목록 가져오기
    @GetMapping("/pending")
    public ResponseEntity<List<AuctionResDto>> getPendingList() {
        log.info("요청받음");
        return ResponseEntity.ok(auctionService.getPendingList());
    }

//    // 대규모 경매 승인 및 일정 배정
//    @PutMapping("/approve/{id}")
//    public ResponseEntity<Boolean> approveAuction(@PathVariable Long id, @RequestBody LocalDateTime starttime) {
//        return ResponseEntity.ok()
//    }
}
