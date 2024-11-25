//package com.wrongweather.moipzy.global;
//
//import com.wrongweather.moipzy.domain.jwt.JwtTokenFilter;
//import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class Webconfig implements WebMvcConfigurer {
//
//    private final JwtTokenUtil jwtTokenUtil;
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
//}
