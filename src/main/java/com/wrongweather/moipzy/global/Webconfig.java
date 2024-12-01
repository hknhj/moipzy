package com.wrongweather.moipzy.global;

//import com.wrongweather.moipzy.domain.jwt.JwtTokenFilter;
import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Webconfig implements WebMvcConfigurer {

//  private final JwtTokenUtil jwtTokenUtil;
//
//    @Autowired
//    public Webconfig(JwtTokenUtil jwtTokenUtil) {
//        this.jwtTokenUtil = jwtTokenUtil;
//    }
//
//    @Bean
//    public FilterRegistrationBean<OncePerRequestFilter> jwtFiler() {
//        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
//        //registrationBean.setFilter(new JwtTokenFilter(jwtTokenUtil));
//        //registrationBean.addUrlPatterns("/*");  // 모든 요청에 필터 적용
//        return registrationBean;
//    }

    // Spring 서버 전역적으로 CORS 설정
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("*") // “*“같은 와일드카드를 사용
                    .allowedMethods("GET", "POST") // 허용할 HTTP method
                    .allowCredentials(true); // 쿠키 인증 요청 허용
        }
    }
}
