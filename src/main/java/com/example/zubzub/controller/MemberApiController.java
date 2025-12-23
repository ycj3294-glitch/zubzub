package com.example.zubzub.controller;

import com.example.zubzub.dto.*;
import com.example.zubzub.repository.MemberRepository;
import com.example.zubzub.security.CustomUserDetails;
import com.example.zubzub.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;

import java.time.Duration;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberApiController {

    private final MemberService memberService;
    private final MailService mailService;
    private final MemberRepository memberRepository;

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
    public ResponseEntity<String> signupWithMail(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody MemberSignupReqDto req) {

        // "Bearer <token>" 형태로 들어오므로 앞의 "Bearer " 제거
        String token = authorizationHeader.replace("Bearer ", "");
        String code  = req.getCode();

        // 이메일 중복 체크
        if (memberService.isEmailExists(req.getEmail())) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }
//        memberService.savePendingMember(req);
//        String code = String.format("%06d", new Random().nextInt(999999));
//        mailService.sendVerificationEmailHtml(req.getEmail(), code);
//        String token = JwtUtil.generateSignupToken(req.getEmail(), code);

        // null 체크
        if (code == null) {
            return ResponseEntity.badRequest().body("인증번호가 누락되었습니다.");
        }

        // 토큰 검증
        boolean isValid = JwtUtil.validateToken(token, code);

        if (isValid) {
            memberService.completeSignup(req); // 실제 회원가입 처리
            return ResponseEntity.ok("회원가입 성공");
        }
        return ResponseEntity.badRequest().body("인증 정보가 유효하지 않습니다.");
    }


    @PostMapping("/signup/verify")
    public ResponseEntity<Boolean> verifySignupCode(@RequestBody SignupVerifyDto req) {
        boolean valid = JwtUtil.validateToken(req.getToken(), req.getCode());
        return ResponseEntity.ok(valid);
    }
    @PostMapping("/signup/complete")
    public ResponseEntity<String> completeSignup(@RequestBody MemberSignupReqDto req) {
        memberService.completeSignup(req); // Service 호출
        return ResponseEntity.ok("회원가입 완료");
    }


    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginMemberDto> login(@RequestBody LoginDto req,
                                                HttpServletResponse response) {
        log.info("로그인 요청: {}", req);

        // 서비스에서 로그인 처리
        LoginMemberDto result = memberService.loginWithPwd(req.getEmail(), req.getPwd());
        if (result == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // RefreshToken → HttpOnly 쿠키에 저장
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false) // 운영환경은 true, 개발환경은 false
                .path("/")
                .sameSite("Lax")
                .domain("localhost")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 클라이언트에는 RefreshToken을 내려주지 않음
        result.setRefreshToken(null);

        log.info("[LOGIN] AccessToken 발급 완료");
        return ResponseEntity.ok(result);
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {

        // RefreshToken 쿠키 삭제 (SameSite 옵션 포함)
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // 운영환경은 true, 개발환경은 false
                .path("/")
                .sameSite("Lax")
                .domain("localhost")
                .maxAge(0) // 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        log.info("[LOGOUT] RefreshToken 삭제 완료");

        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/me")
    public ResponseEntity<LoginMemberDto> checkLogin (Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        CustomUserDetails cu = (CustomUserDetails) authentication.getPrincipal();
        LoginMemberDto lm = new LoginMemberDto();
        lm.setId(cu.getId());
        lm.setEmail(cu.getEmail());
        lm.setName(cu.getName());
        lm.setNickname(cu.getNickname());
        return ResponseEntity.ok(lm);
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

            String newAccessToken = JwtUtil.generateLoginToken(email, memberId, isAdmin);

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
