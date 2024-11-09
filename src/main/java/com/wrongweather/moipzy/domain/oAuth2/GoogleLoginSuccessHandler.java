package com.wrongweather.moipzy.domain.oAuth2;

import com.wrongweather.moipzy.domain.jwt.JwtToken;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
         OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

         String email = oAuth2User.getAttribute("email");
         User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException());

//         System.out.println("login success handler email: " + email);
//        System.out.println("login success handler email: " + user.getEmail());

        log.info("login success, email: {}, user: {}", email, user);

        if (user != null) {
            JwtToken token = jwtTokenUtil.createToken(user.getUserId(), user.getEmail(), user.getUsername());
            String accessToken = token.getAccessToken();
//            System.out.println("access Token: " + accessToken);
            log.info("login success, accessToken: {}", accessToken);
            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        }
    }
}
