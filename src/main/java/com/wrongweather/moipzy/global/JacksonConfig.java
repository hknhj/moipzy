package com.wrongweather.moipzy.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 공통 설정 추가
        mapper.findAndRegisterModules(); // Java 8 모듈 자동 등록 (예: JavaTimeModule)
        return mapper;
    }
}
