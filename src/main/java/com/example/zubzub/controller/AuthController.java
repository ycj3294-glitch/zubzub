package com.example.zubzub.controller;

import com.example.zubzub.dto.LoginReqDto;
import com.example.zubzub.dto.SignUpReqDto;
import com.example.zubzub.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // 내부적으로 JSON 직렬화/역직렬화 기능이 있음
@CrossOrigin(origins = {"http://192.168.0.93:3000", "http://localhost:3000"}) // 동일 출처 에러 방지용
@RequestMapping("/auth")    // 진입경로, 인증과 관련된 경로를 별도 관리하기 위함, 인가 없이 진입되는 경로
@RequiredArgsConstructor    // 매개변수가 전부 있는 생성자에서 의존성 주입
public class AuthController {
    private final AuthService authService;

    @GetMapping("/exists/{email}")  // 회원 가입 여부 확인, Path Variable 방식으로 email 전달받음
    public ResponseEntity<Boolean> memberExists(@PathVariable String email) {
        log.info("email : {}", email);
        return ResponseEntity.ok(authService.isMember(email));   // 그대로 멤버가 존재한다는 의미로 리턴
    }

    @PostMapping("/signup") // POST 방식으로 회원 가입
    public ResponseEntity<Boolean> signup(@RequestBody SignUpReqDto dto) {
        return ResponseEntity.ok(authService.signUp(dto));
    }

    @PostMapping("/login")   // POST 방식으로 로그인
    public ResponseEntity<Boolean> login(@RequestBody LoginReqDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
