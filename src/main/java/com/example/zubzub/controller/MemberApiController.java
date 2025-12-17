package com.example.zubzub.controller;

import com.example.zubzub.dto.*;
import com.example.zubzub.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;

import java.util.Map;
import java.util.Random;

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
            @RequestBody String password) {

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
        String code = String.format("%06d", new Random().nextInt(999999));
        mailService.sendVerificationEmailHtml(req.getEmail(), code);
        String token = JwtUtil.generateToken(req.getEmail(), code);

        return ResponseEntity.ok(token);
    }


    @PostMapping("/signup/verify")
    public ResponseEntity<Boolean> verifySignupCode(
            @RequestBody SignupVerifyDto req) {

        boolean valid = JwtUtil.validateToken(req.getToken(), req.getCode());

        if (valid) {
            memberService.activateMember(
                    JwtUtil.getEmail(req.getToken())
            );
        }

        return ResponseEntity.ok(valid);
    }




    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginMemberDto> login(@RequestBody LoginDto req, HttpServletResponse response) {
        LoginMemberDto result = memberService.loginWithJwt(req.getEmail(), req.getPwd());
        if(result == null) return ResponseEntity.status(401).build();

        log.info("[LOGIN] AccessToken: {}", result.getAccessToken());
        log.info("[LOGIN] RefreshToken (쿠키용): {}", result.getRefreshToken());

        Cookie refreshCookie = new Cookie("refreshToken", result.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);

        result.setRefreshToken(null); // 클라이언트에 body로는 안 내려주고 쿠키로만 전달
        return ResponseEntity.ok(result);
    }
    @GetMapping("/token/refresh")
    public ResponseEntity<String> refreshAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            log.warn("[TOKEN REFRESH] 리프레시 토큰 없음. AccessToken 재발급 불가");
            return ResponseEntity.status(401).body("리프레시 토큰 없음");
        }

        try {
            Jws<Claims> claims = JwtUtil.parseToken(refreshToken);
            String email = claims.getBody().getSubject();
            long memberId = claims.getBody().get("memberId", Long.class);
            boolean isAdmin = "ADMIN".equals(claims.getBody().get("role", String.class));

            String newAccessToken = JwtUtil.generateTokenForLogin(email, memberId, isAdmin);

            log.info("[TOKEN REFRESH] 자동 재발급 성공: memberId={}, email={}, newAccessToken={}",
                    memberId, email, newAccessToken);

            return ResponseEntity.ok(newAccessToken);

        } catch (JwtException e) {
            log.warn("[TOKEN REFRESH] 리프레시 토큰 만료 또는 유효하지 않음: {}", e.getMessage());
            return ResponseEntity.status(401).body("리프레시 토큰 만료 또는 유효하지 않음");
        }
    }




    // 비밀번호 찾기 요청 (메일 발송)
    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetRequestDto req) {
        String code = memberService.sendPasswordResetCode(req.getEmail());
        if(code == null) return ResponseEntity.badRequest().body("해당 이메일이 없습니다.");
        return ResponseEntity.ok(code);
    }

    // 비밀번호 재설정
    @PostMapping("/password-reset/verify")
    public ResponseEntity<Boolean> verifyPasswordReset(@RequestBody PasswordResetDto req) {
        boolean success = memberService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return ResponseEntity.ok(success);
    }





}
