package com.wrongweather.moipzy.domain.users;

import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.dto.UserLoginRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.dto.UserRegisterResponseDto;
import com.wrongweather.moipzy.domain.users.exception.EmailAlreadyExistsException;
import com.wrongweather.moipzy.domain.users.exception.LoginFailedException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        UserRegisterResponseDto userRegisterResponseDto = userService.register(userRegisterRequestDto);

        //then
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(1, userRegisterResponseDto.getUserId());
        assertEquals("testemail3@naver.com", userRegisterResponseDto.getEmail());
        assertEquals("testname", userRegisterResponseDto.getUsername());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도")
    void tryRegisterWithAlreadyRegisteredEmail() {
        // given
        User existingUser = User.builder()
                .email("qwer@naver.com")
                .password("1234")
                .username("nick")
                .build();

        given(userRepository.findByEmail(existingUser.getEmail())).willReturn(Optional.of(existingUser));

        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .email(existingUser.getEmail())
                .password("asdf")
                .username("nick")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.register(userRegisterRequestDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
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

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인")
    void loginWithNotExistingEmail() {
        // given
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email("notExisting@naver.com")
                .password("1234")
                .build();

        given(userRepository.findByEmail(userLoginRequestDto.getEmail())).willReturn(Optional.empty());


        // when & then
        assertThatThrownBy(() -> userService.login(userLoginRequestDto))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인")
    void loginWithWrongPassword() {
        // given
        User existingUser = User.builder()
                .email("qwer@naver.com")
                .password("1234")
                .username("nick")
                .build();

        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .email(existingUser.getEmail())
                .password("asdf")
                .build();

        given(userRepository.findByEmail(existingUser.getEmail())).willReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.login(userLoginRequestDto))
                .isInstanceOf(LoginFailedException.class);
    }
}
