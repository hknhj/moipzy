package com.wrongweather.moipzy.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserRegisterResponseDto {
    private int userId;
    private String email;
    private String username;
}
