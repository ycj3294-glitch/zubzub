package com.example.zubzub.controller;

import com.example.zubzub.security.JwtUtil;
import com.example.zubzub.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;


    // 인증번호 발송 + JWT 생성
    @PostMapping("/send")
    public ResponseEntity<String> sendMail(@RequestBody String email) {
        String code = mailService.sendVerificationEmail(email);
        String token = JwtUtil.generateToken(email, code);
        return ResponseEntity.ok(token); // JWT를 클라이언트에 전달
    }

    // 인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyCode(@RequestBody String token, @RequestBody String code) {
        boolean valid = JwtUtil.validateToken(token, code);
        return ResponseEntity.ok(valid);
    }
}
