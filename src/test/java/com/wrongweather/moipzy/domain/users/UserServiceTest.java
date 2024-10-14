package com.wrongweather.moipzy.domain.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.controller.AuthController;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthController authController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @Transactional
    @Rollback(true)
    @DisplayName("회원가입 성공")
    void 회원가입() {
        //given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email("testemail2@naver.com")
                .password("test")
                .username("testname")
                .build();

        //when
        UserIdResponseDto register = userService.register(userRegisterRequestDto);

        //then
        User user = userRepository.findByUserId(register.getUserId());
        assertEquals(user.getEmail(), userRegisterRequestDto.getEmail());
        Assertions.assertTrue(passwordEncoder.matches(userRegisterRequestDto.getPassword(), user.getPassword()));
        assertEquals(user.getUsername(), userRegisterRequestDto.getUsername());
    }

    @Test
    @Transactional
    @Rollback(true)
    @DisplayName("로그인 성공")
    void 로그인() {
        //given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email("temail@naver.com")
                .password("tpwd")
                .username("testname")
                .build();

        User user = userRepository.findByUserId(userService.register(userRegisterRequestDto).getUserId());

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email(user.getEmail())
                .password("tpwd")
                .build();

        //when
        User loginedUser = userService.login(userLoginRequestDto);

        //then
        assertEquals(loginedUser.getEmail(), userRegisterRequestDto.getEmail());
        Assertions.assertTrue(passwordEncoder.matches(userRegisterRequestDto.getPassword(), loginedUser.getPassword()));
        assertEquals(loginedUser.getUsername(), userRegisterRequestDto.getUsername());
    }

    @Test
    @Transactional
    @Rollback(true)
    @DisplayName("JWT 토큰 발행 성공")
    void JWT토큰발행() throws Exception {
        //given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email("temail@naver.com")
                .password("tpwd")
                .username("testname")
                .build();

        User user = userRepository.findByUserId(userService.register(userRegisterRequestDto).getUserId());

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email(user.getEmail())
                .password("tpwd")
                .build();

        User loginedUser = userService.login(userLoginRequestDto);

        //when
        MvcResult result = mockMvc.perform(post("/moipzy/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        //응답 헤더에서 JWT token 추출
        String token = result.getResponse().getHeader("Authorization").replace("Bearer ", "");

        // JWT 토큰에서 userId 추출
        Claims claims = JwtTokenUtil.extractClaims(token);
        Integer userId = (Integer) claims.get("userId");
        String email = (String) claims.get("email");
        String username = (String) claims.get("username");

        // userId가 null이 아닌지 확인
        assertEquals(loginedUser.getUserId(), userId);
    }
}
