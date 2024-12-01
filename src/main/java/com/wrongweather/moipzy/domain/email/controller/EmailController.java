package com.wrongweather.moipzy.domain.email.controller;

import com.wrongweather.moipzy.domain.email.dto.EmailPostDto;
import com.wrongweather.moipzy.domain.email.dto.VerificationDto;
import com.wrongweather.moipzy.domain.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/send-mail")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입 이메일 인증코드 발송
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody EmailPostDto emailPostDto) {
        // 이메일로 인증번호 보내기
        String verificationCode = emailService.sendVerificationEmail(emailPostDto.getEmail());

        return ResponseEntity.ok(verificationCode);
    }

    // 인증번호 확인
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody VerificationDto verificationDto) {
        // 입력한 인증번호와 DB에 저장된 인증번호를 비교하는 로직 추가
        if (isVerificationCodeValid(verificationDto.getEmail(), verificationDto.getVerificationCode())) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패");
        }
    }

    // 인증번호 유효성 검사
    private boolean isVerificationCodeValid(String email, String verificationCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        return storedCode != null && storedCode.equals(verificationCode);
    }
}
