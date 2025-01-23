package com.wrongweather.moipzy.domain.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.users.controller.UserController;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterResponseDto;
import com.wrongweather.moipzy.domain.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
