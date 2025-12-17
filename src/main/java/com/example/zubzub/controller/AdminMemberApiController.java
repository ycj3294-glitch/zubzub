package com.example.zubzub.controller;

import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberApiController {

    private final MemberService memberService;

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
}
