package com.wrongweather.moipzy.domain.users.service;

import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.global.exception.LoginFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserIdResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        String encodedPassword = encoder.encode(userRegisterRequestDto.getPassword());

        return UserIdResponseDto.builder()
                .userId(userRepository.save(userRegisterRequestDto.toEntity(encodedPassword)).getUserId())
                .build();
    }

    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        String requestEmail = userLoginRequestDto.getEmail();
        String requestPassword = userLoginRequestDto.getPassword();

        User foundUser = userValid(requestEmail).orElseThrow(LoginFailedException::new);

        if(!encoder.matches(requestPassword, foundUser.getPassword())) {
            throw new LoginFailedException();
        }
        return new UserLoginResponseDto();
    }

    public Optional<User> userValid(String email) {
        return userRepository.findByEmail(email);
    }
}
