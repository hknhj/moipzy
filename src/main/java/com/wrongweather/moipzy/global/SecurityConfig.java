package com.wrongweather.moipzy.global;

//import com.wrongweather.moipzy.domain.jwt.JwtTokenFilter;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
//import com.wrongweather.moipzy.domain.oAuth2.CustomOAuth2UserService;
//import com.wrongweather.moipzy.domain.oAuth2.GoogleLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenUtil jwtTokenUtil;
    //private final CustomOAuth2UserService customOAuth2UserService;
    //private final GoogleLoginSuccessHandler googleLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //REST API이므로 basic auth 및 csrf, cors을 disable함
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) //스프링 시큐리티에서 제공하는 로그인 방법, JSON을 이용해서 로그인 할 것이기 때문에 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                //권한 부여
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers("/moipzy/users/register", "/moipzy/users/login/**").permitAll() // "/login/**", "/favicon.ico"
//                        .requestMatchers("/swagger/**", "/swagger-ui.html/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() //스웨거 필터 제외
//                        .requestMatchers(HttpMethod.POST, "moipzy/**").authenticated()
//                        .anyRequest().authenticated()
//                        //.anyRequest().permitAll()
//                )

                //oauth2 설정
//                .oauth2Login(oauth2 ->
//                        oauth2.userInfoEndpoint(c -> c.userService(customOAuth2UserService))
//                                .successHandler(googleLoginSuccessHandler)
//                )

                //JWT 토큰 사용하므로 세션 사용 안함
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .addFilterBefore(new JwtTokenFilter(jwtTokenUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
