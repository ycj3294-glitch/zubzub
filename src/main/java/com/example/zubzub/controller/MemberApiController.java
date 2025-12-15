package com.example.zubzub.controller;

import com.example.zubzub.dto.LoginDto;
import com.example.zubzub.dto.MemberResDto;
import com.example.zubzub.dto.MemberSignupReqDto;
import com.example.zubzub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberApiController {

    private final MemberService memberService;
    private final MailService mailService;

    /**
     * 이메일 중복 체크
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = memberService.isEmailExists(email);
        log.info("[API] 이메일 중복 체크: {} / {}", email, exists);
        return ResponseEntity.ok(exists);
    }

    /**
     * 닉네임 중복 체크
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = memberService.isNicknameExists(nickname);
        log.info("[API] 닉네임 중복 체크: {} / {}", nickname, exists);
        return ResponseEntity.ok(exists);
    }

    /**
     * 특정 회원 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberResDto> getMember(@PathVariable Long id) {
        MemberResDto member = memberService.getById(id);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        log.info("[API] 회원 조회: {}", member.getEmail());
        return ResponseEntity.ok(member);
    }

    /**
     * 비밀번호 검증 (회원정보 수정 전)
     */
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<Boolean> verifyPassword(
            @PathVariable Long id,
            @RequestParam String password) {

        boolean match = memberService.checkPassword(id, password);
        log.info("[API] 비밀번호 검증: memberId={} / result={}", id, match);

        return ResponseEntity.ok(match);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signupWithMail(@RequestBody MemberSignupReqDto req) {
        if(memberService.isEmailExists(req.getEmail())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        memberService.savePendingMember(req);

        // MailService 인스턴스로 호출
        String code = mailService.sendVerificationEmailHtml(req.getEmail());
        System.out.println("보낼 코드: "+code);
        String token = JwtUtil.generateToken(req.getEmail(), code);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<Boolean> verifySignupCode(@RequestParam String token, @RequestParam String code) {
        boolean valid = JwtUtil.validateToken(token, code);
        if(valid) {
            memberService.activateMember(JwtUtil.getEmail(token));
        }
        return ResponseEntity.ok(valid);
    }



    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<MemberResDto> login(@RequestBody LoginDto req) {

        MemberResDto result = memberService.login(req.getEmail(), req.getPwd());

        if (result == null) {
            log.warn("[API] 로그인 실패: {}", req.getEmail());
            return ResponseEntity.status(401).build();
        }

        log.info("[API] 로그인 성공: {}", req.getEmail());
        return ResponseEntity.ok(result);
    }
}
