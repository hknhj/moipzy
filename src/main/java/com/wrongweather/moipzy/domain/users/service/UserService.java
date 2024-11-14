package com.wrongweather.moipzy.domain.users.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wrongweather.moipzy.domain.jwt.JwtToken;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.service.TemperatureService;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final TemperatureService temperatureService;
    private final JwtTokenUtil jwtTokenUtil;
    private final Environment env;

    private final RestTemplate restTemplate = new RestTemplate();

    public UserIdResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {

        String encodedPassword = null;
        if (userRegisterRequestDto.getPassword() != null) {
            encodedPassword = encoder.encode(userRegisterRequestDto.getPassword());
        }

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

    public String socialLogin(String code) {
        String accessToken = getAccessToken(code);
        JsonNode userResourceNode = getUserResource(accessToken);
        //System.out.println("userResourceNode = " + userResourceNode);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String nickname = userResourceNode.get("name").asText();

        TemperatureRange range = temperatureService.setDefaultRange();

        // 이메일로 찾았을 때 있으면 반환하고, 없으면 유저 등록
        // 원래는 orElse만 사용했었는데, orElse는 값을 생성하기 전에 항상 실행되기 때문에, 무조건 실행됨
        // 따라서 orElseGet을 사용하면 Optional이 비어 있을 때에만 실행되므로 유저가 없을 때만 새로운 유저를 저장할 수 있습니다.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .range(range)
                        .password(null)
                        .username(nickname)
                        .build()));

        JwtToken token = jwtTokenUtil.createToken(user.getUserId(), user.getEmail(), user.getUsername(), "google", accessToken);
        return token.getAccessToken();
    }

    private String getAccessToken(String code) {
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

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

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
