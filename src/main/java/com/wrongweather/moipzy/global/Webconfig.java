package com.wrongweather.moipzy.global;

import com.wrongweather.moipzy.domain.jwt.JwtTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class Webconfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> jwtFiler() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtTokenFilter());
        registrationBean.addUrlPatterns("/*");  // 모든 요청에 필터 적용
        return registrationBean;
    }
}
