package com.wrongweather.moipzy.domain.email.dto;

import lombok.Getter;

@Getter
public class VerificationDto {
    private String email;
    private String verificationCode;
}
