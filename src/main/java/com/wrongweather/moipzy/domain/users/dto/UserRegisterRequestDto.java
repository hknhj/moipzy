package com.wrongweather.moipzy.domain.users.dto;

import com.wrongweather.moipzy.domain.users.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {
    private String email;
    private String password;
    private String username;

    public User toEntity(String password) {
        return User.builder()
                .email(email)
                .password(password)
                .username(username)
                .build();
    }
}
