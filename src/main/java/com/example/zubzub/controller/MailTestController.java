package com.example.zubzub.controller;

import com.example.zubzub.service.MailService;
import lombok.RequiredArgsConstructor;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailTestController {


    private final MailService mailService;

    @GetMapping("/mail/test")
    public String test() {
        String email = "Bureum66@gmail.com"; // 테스트용
        String code = mailService.sendVerificationEmail(email); // MailService로 코드 생성 + 메일 발송
        return "인증번호가 발송되었습니다: " + code; // 콘솔/브라우저 확인용
    }

}
