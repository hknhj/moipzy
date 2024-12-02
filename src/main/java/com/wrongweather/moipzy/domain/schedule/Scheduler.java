package com.wrongweather.moipzy.domain.schedule;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import com.wrongweather.moipzy.domain.token.service.TokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final TokenService tokenService;
    private final CalendarService calendarService;

    @PostConstruct
    public void init() {
        log.info("Token refresh and get calendar events");
        tokenService.refreshTokens();
        calendarService.getAllEvents();
    }
}
