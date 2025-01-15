package com.wrongweather.moipzy.domain.users;

import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.dto.UserIdResponseDto;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder encoder;
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    public void setUp() {
        encoder = new BCryptPasswordEncoder();
        String secretKey = "a97B5InEzfDn9CAfguLdaQvquXe5MswNw-no79Bq8tU=";
        jwtTokenUtil = new JwtTokenUtil(secretKey);
        ReflectionTestUtils.setField(userService, "encoder", encoder);
        ReflectionTestUtils.setField(userService, "jwtTokenUtil", jwtTokenUtil);
    }

    @Test
    @DisplayName("회원가입")
    void saveUser() {
        //given
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email("testemail3@naver.com")
                .password("test")
                .username("testname")
                .build();

        User user = User.builder()
                .email(userRegisterRequestDto.getEmail())
                .password(encoder.encode(userRegisterRequestDto.getPassword()))
                .username(userRegisterRequestDto.getUsername())
                .build();
        user.setId(1);

        when(userRepository.save(any(User.class))).thenReturn(user);

        //when
        UserIdResponseDto userIdResponseDto = userService.register(userRegisterRequestDto);

        //then
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(1, userIdResponseDto.getUserId());
    }

    @Test
    @DisplayName("로그인")
    void 로그인() {
        //given
        User existingUser = User.builder()
                .email("testemail@example.com")
                .password(encoder.encode("password123"))
                .username("testuser")
                .build();

        given(userRepository.findByEmail("testemail@example.com")).willReturn(Optional.of(existingUser));

        //로그인 정보
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("testemail@example.com")
                .password("password123")
                .build();

        //when
        List<String> tokenAndUsername = userService.login(userLoginRequestDto);

        //then
        assertNotNull(tokenAndUsername);
        assertNotNull(tokenAndUsername.get(0), "토큰이 반환되어야 합니다.");
        assertTrue(tokenAndUsername.get(0).startsWith("eyJ"), "JWT 토큰이어야 합니다.");
        assertEquals(existingUser.getUsername(), tokenAndUsername.get(1), "유저 이름이 같지 않습니다.");
    }
}
