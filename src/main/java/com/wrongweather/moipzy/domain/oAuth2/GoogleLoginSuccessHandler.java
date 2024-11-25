//package com.wrongweather.moipzy.domain.oAuth2;
//
//import com.wrongweather.moipzy.domain.jwt.JwtToken;
//import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
//import com.wrongweather.moipzy.domain.users.User;
//import com.wrongweather.moipzy.domain.users.UserRepository;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class GoogleLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
//    private final JwtTokenUtil jwtTokenUtil;
//    private final UserRepository userRepository;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        String email = oAuth2User.getAttribute("email");
//
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException());
//
//        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(
//                "google", authentication.getName());
//
//        //google access token
//        String googleAccessToken = authorizedClient.getAccessToken().getTokenValue();
//
//        if (user != null) {
//            JwtToken token = jwtTokenUtil.createToken(user.getUserId(), user.getEmail(), user.getUsername(), "google", googleAccessToken);
//            String accessToken = token.getAccessToken();
////            response.setHeader("Authorization", "Bearer " + accessToken);
////            response.setStatus(HttpServletResponse.SC_OK);
//
//            log.info("Login success: email={},  jwtAccessToken={}, googleAccessToken={}", email, accessToken, googleAccessToken);
//
//            // 프론트엔드로 리디렉션하여 JWT와 구글 액세스 토큰을 전달
//            String redirectUrl = "http://localhost:8080/moipzy/success?jwtToken=" + accessToken + "&googleToken" + googleAccessToken;
//            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
//         } else {
//             response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
//         }
//    }
//}
