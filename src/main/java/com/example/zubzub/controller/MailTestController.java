package com.example.zubzub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailTestController {

    private final JavaMailSender mailSender;

    @GetMapping("/mail/test")
    public String test() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("받는사람@gmail.com");
        message.setSubject("SMTP 테스트");
        message.setText("메일 간다");
        message.setFrom("yourgmail@gmail.com");

        mailSender.send(message);
        return "OK";
    }
}
