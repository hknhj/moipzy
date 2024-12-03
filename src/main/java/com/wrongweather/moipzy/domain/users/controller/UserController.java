package com.wrongweather.moipzy.domain.users.controller;

import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/moipzy/users", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.redirect-uri}")
    private String redirectUri;

    // 일반 회원가입 진행
    @PostMapping("/register")
    public String register(@Validated @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        userService.register(userRegisterRequestDto);
        return "redirect:/home";  // 홈 화면으로 리디렉션
    }

    // 일반 로그인 진행
    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequestDto userLoginRequestDto, HttpServletResponse response) throws IOException {
        List<String> accessTokenAndName = userService.login(userLoginRequestDto);

        String redirectUrl = "/loginmypage?token=" + accessTokenAndName.get(0) + "&username=" + accessTokenAndName.get(1);
        response.sendRedirect(redirectUrl);
    }

    // 구글 로그인으로 리디렉션 되도록 만드는 컨트롤러
    @GetMapping("/google")
    public void redirectToGoogleAuth(HttpServletResponse response) throws IOException {
        // Google OAuth2 인증 URL 구성
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth?"
                + "client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&access_type=offline"
                + "&response_type=code"
                + "&scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/calendar.readonly"
                + "&prompt=consent";

        // Google OAuth2 페이지로 리다이렉트
        response.sendRedirect(googleAuthUrl);
    }

    //구글 로그인 진행 후 code 를 포함하여 redirection 되는 url
    @GetMapping("/login/google")
    public void googleLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        List<String> jwtTokenAndName = userService.socialLogin(code);

        String redirectUrl = "/loginmypage?token=" + jwtTokenAndName.get(0) + "&username=" + jwtTokenAndName.get(1);
        response.sendRedirect(redirectUrl);
    }
}
