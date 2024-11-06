package com.wrongweather.moipzy.domain.users.dto;

import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.users.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserRegisterRequestDto {
    private String email;
    private String password;
    private String username;

    @Builder
    public UserRegisterRequestDto(String password, String username, String email) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public User toEntity(String password, TemperatureRange range) {
        return User.builder()
                .email(email)
                .password(password)
                .range(range)
                .username(username)
                .build();
    }
}
