package com.wrongweather.moipzy.domain.email.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    // 이메일 인증번호 생성
    public String sendVerificationEmail(String toEmail) {
        // 인증번호를 랜덤으로 생성
        String verificationCode = generateVerificationCode();

        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage(), false, "UTF-8");
            message.setTo(toEmail);
            message.setSubject("회원가입 인증번호");
            message.setText("회원가입을 위한 인증번호는 " + verificationCode + " 입니다.");
            message.setFrom("your-email@gmail.com");  // 보내는 사람 이메일
            log.info("verification code: {}", verificationCode);
            mailSender.send(message.getMimeMessage());

            //redis에 5분동안 저장
            redisTemplate.opsForValue().set(toEmail, verificationCode, 5, TimeUnit.MINUTES);

            //log 출력
            log.info("Stored verification code for {} in Redis: {}", toEmail, redisTemplate.opsForValue().get(toEmail));


            return verificationCode;

        } catch (MessagingException e) {
            e.printStackTrace();
            return null;  // 오류가 발생한 경우 null 반환
        }
    }

    // 1000~9999 사이의 인증번호 생성
    private String generateVerificationCode() {
        int code = (int) (Math.random() * 9000) + 1000;  // 1000 ~ 9999 사이의 인증번호
        return String.valueOf(code);
    }
}
