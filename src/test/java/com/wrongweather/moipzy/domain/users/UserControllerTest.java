package com.wrongweather.moipzy.domain.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.exception.ErrorResponse;
import com.wrongweather.moipzy.domain.users.controller.UserController;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterResponseDto;
import com.wrongweather.moipzy.domain.users.exception.LoginFailedException;
import com.wrongweather.moipzy.domain.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userRegister() throws Exception {
        // given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email("testemail@naver.com")
                .username("test")
                .password("password123")
                .build();

        UserRegisterResponseDto userRegisterResponseDto = UserRegisterResponseDto.builder()
                .userId(1)
                .email("testemail@naver.com")
                .username("test")
                .build();
        given(userService.register(any(UserRegisterRequestDto.class))).willReturn(userRegisterResponseDto);

        //when
        //then
        mockMvc.perform(post("/moipzy/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("testemail@naver.com"))
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    void userLogin() throws Exception {
        // given
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("testemail@naver.com")
                .password("test")
                .build();

        String expectedToken = "access token";
        String expectedUsername = "Nick";
        String encodedUsername = URLEncoder.encode(expectedUsername, StandardCharsets.UTF_8);

        given(userService.login(any(UserLoginRequestDto.class))).willReturn(List.of("access token", "Nick"));

        // when & then
        mockMvc.perform(post("/moipzy/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + expectedToken))
                .andExpect(content().string(encodedUsername));
    }

    @Test
    void userLoginFail() throws Exception {
        // given
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("testfail@naver.com")
                .password("test")
                .build();

        given(userService.login(any(UserLoginRequestDto.class))).willThrow(new LoginFailedException());

        // when & then
        mockMvc.perform(post("/moipzy/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Wrong id or password"))));
    }
}
