package com.wrongweather.moipzy.domain.users.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wrongweather.moipzy.domain.jwt.JwtToken;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.token.Token;
import com.wrongweather.moipzy.domain.token.TokenRepository;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.global.exception.LoginFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final Environment env;

    private final RestTemplate restTemplate = new RestTemplate();

    // 회원가입 서비스
    public UserIdResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {

        String encodedPassword = null;
        if (userRegisterRequestDto.getPassword() != null) {
            encodedPassword = encoder.encode(userRegisterRequestDto.getPassword());
        }

        return UserIdResponseDto.builder()
                .userId(userRepository.save(userRegisterRequestDto.toEntity(encodedPassword)).getUserId())
                .build();
    }

    // 로그인 후 jwt token 발행 서비스
    public String login(UserLoginRequestDto userLoginRequestDto) {
        String requestEmail = userLoginRequestDto.getEmail();
        String requestPassword = userLoginRequestDto.getPassword();

        User foundUser = userValid(requestEmail).orElseThrow(LoginFailedException::new);

        if(!encoder.matches(requestPassword, foundUser.getPassword())) {
            throw new LoginFailedException();
        }

        JwtToken token = jwtTokenUtil.createToken(foundUser.getUserId(), foundUser.getEmail(), foundUser.getUsername(), "regular", null);
        return token.getAccessToken();
    }

    // 구글 로그인 서비스
    @Transactional
    public String socialLogin(String code) {
        List<String> tokens = getAccessToken(code);
        String accessToken = tokens.get(0);
        String refreshToken = tokens.get(1);
        JsonNode userResourceNode = getUserResource(accessToken);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String nickname = userResourceNode.get("name").asText();

        // 이메일로 찾았을 때 있으면 반환하고, 없으면 유저 등록
        // 원래는 orElse만 사용했었는데, orElse는 값을 생성하기 전에 항상 실행되기 때문에, 무조건 실행됨
        // 따라서 orElseGet을 사용하면 Optional이 비어 있을 때에만 실행되므로 유저가 없을 때만 새로운 유저를 저장할 수 있습니다.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .password(null)
                        .username(nickname)
                        .build())
                );

        Token extractedToken = Token.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        tokenRepository.findByUserId(user.getUserId())
                .orElseGet(() -> tokenRepository.save(extractedToken));

        JwtToken googleToken = jwtTokenUtil.createToken(user.getUserId(), user.getEmail(), user.getUsername(), "google", accessToken);
        return googleToken.getAccessToken();
    }

    // 리디렉션된 code를 가지고 구글의 access_token을 추출하는 함수
    private List<String> getAccessToken(String code) {
        String clientId = env.getProperty("oauth2.google.client-id");
        String clientSecret = env.getProperty("oauth2.google.client-secret");
        String redirectUri = env.getProperty("oauth2.google.redirect-uri");
        String tokenUri = env.getProperty("oauth2.google.token-uri");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, entity, Map.class);

        // Access Token 및 Refresh Token 저장
        Map<String, Object> responseBody = response.getBody();
        String accessToken = (String) responseBody.get("access_token");
        String refreshToken = (String) responseBody.get("refresh_token");

        return Arrays.asList(accessToken, refreshToken);
    }

    // 구글 로그인을 통해 유저의 정보를 얻어오는 함수
    private JsonNode getUserResource(String accessToken) {
        String resourceUri = env.getProperty("oauth2.google.resource-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }

    private Optional<User> userValid(String email) {
        return userRepository.findByEmail(email);
    }
}
