package com.wrongweather.moipzy.domain.token;

import com.wrongweather.moipzy.domain.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenScheduler {
    private final TokenService tokenService;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행 (밀리초 단위)
    public void refreshTokensPeriodically() {
        tokenService.refreshTokens();
    }
}
