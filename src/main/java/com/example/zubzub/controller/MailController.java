package com.example.zubzub.controller;

import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;


    // 인증번호 발송 + JWT 생성
    @PostMapping("/send")
    public ResponseEntity<String> sendMail(@RequestBody Map<String, String> emailJson) {
        String email = emailJson.get("email");
        log.info("이메일 : {}", email);
        String code = mailService.sendVerificationEmail(email);
        String token = JwtUtil.generateSignupToken(email, code);
        return ResponseEntity.ok(token); // JWT를 클라이언트에 전달
    }

    // 인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> codeJson) {

        String code = codeJson.get("code");

        // "Bearer <token>" 형태로 들어오므로 앞의 "Bearer " 제거
        String token = authorizationHeader.replace("Bearer ", "");

        boolean valid = JwtUtil.validateToken(token, code);
        return ResponseEntity.ok(valid);
    }
}
