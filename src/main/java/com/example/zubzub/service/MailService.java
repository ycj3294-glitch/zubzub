package com.example.zubzub.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final Random random = new Random();

    // 랜덤 6자리 코드 생성
    public String generateCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    // 인증 메일 전송
    public String sendVerificationEmail(String toEmail) {
        String code = generateCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("인증번호 확인");
        message.setText("인증용 코드: " + code);
        message.setFrom("zubzubpq@gmail.com");

        mailSender.send(message);
        return code;
    }
    // HTML 메일 전송
    // 코드 직접 전달
    public void sendVerificationEmailHtml(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("인증번호");
            helper.setText("<h1>인증번호</h1><p>인증번호: <b>" + code + "</b></p>", true);
            helper.setFrom("zubzubpq@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


}
