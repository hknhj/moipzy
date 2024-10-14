package com.wrongweather.moipzy.domain.users.dto;

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

    public User toEntity(String password) {
        return User.builder()
                .email(email)
                .password(password)
                .username(username)
                .build();
    }
}
