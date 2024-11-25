package com.wrongweather.moipzy.global;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi groupUser() {
        return GroupedOpenApi.builder()
                .group("users group")
                .pathsToMatch("/moipzy/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupCloth() {
        return GroupedOpenApi.builder()
                .group("clothes group")
                .pathsToMatch("/moipzy/clothes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupStyle() {
        return GroupedOpenApi.builder()
                .group("style group")
                .pathsToMatch("/moipzy/style/**")
                .build();
    }
}
