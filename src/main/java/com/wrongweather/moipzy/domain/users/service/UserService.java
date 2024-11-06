package com.wrongweather.moipzy.domain.users.service;

import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.TemperatureRepository;
import com.wrongweather.moipzy.domain.temperature.service.TemperatureService;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
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
    private final TemperatureService temperatureService;

    public UserIdResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        String encodedPassword = encoder.encode(userRegisterRequestDto.getPassword());

        TemperatureRange range = temperatureService.setDefaultRange();

        return UserIdResponseDto.builder()
                .userId(userRepository.save(userRegisterRequestDto.toEntity(encodedPassword, range)).getUserId())
                .build();
    }

    public User login(UserLoginRequestDto userLoginRequestDto) {
        String requestEmail = userLoginRequestDto.getEmail();
        String requestPassword = userLoginRequestDto.getPassword();

        User foundUser = userValid(requestEmail).orElseThrow(LoginFailedException::new);

        if(!encoder.matches(requestPassword, foundUser.getPassword())) {
            throw new LoginFailedException();
        }
        return foundUser;
    }

    private Optional<User> userValid(String email) {
        return userRepository.findByEmail(email);
    }
}
