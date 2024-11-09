package com.wrongweather.moipzy.domain.users.controller;

import com.wrongweather.moipzy.domain.jwt.JwtToken;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("moipzy/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public UserIdResponseDto register(@Validated @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return userService.register(userRegisterRequestDto);
    }

//    @PostMapping("/login")
//    public UserLoginResponseDto login(@Validated @RequestBody UserLoginRequestDto userLoginRequestDto) {
//        return userService.login(userLoginRequestDto);
//    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        User user = userService.login(userLoginRequestDto);

        if (user != null) {
            JwtToken token = jwtTokenUtil.createToken(user.getUserId(), user.getEmail(), user.getUsername());
            String accessToken = token.getAccessToken();
            // 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
