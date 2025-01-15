package com.wrongweather.moipzy.global;

import com.wrongweather.moipzy.domain.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil(jwtSecret);
    }
}
