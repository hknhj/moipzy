package com.wrongweather.moipzy.domain.schedule;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import com.wrongweather.moipzy.domain.token.service.TokenService;
import com.wrongweather.moipzy.domain.users.service.UserService;
import com.wrongweather.moipzy.domain.weather.service.WeatherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final TokenService tokenService;
    private final CalendarService calendarService;
    private final UserService userService;
    private final WeatherService weatherService;
    private final StyleService styleService;

    @PostConstruct
    public void init() {
        tokenService.refreshTokens();
        calendarService.getAllEvents();
        userService.getAllKakaoId();
        weatherService.getWeather();
        styleService.getAllStyles();
    }

    // 매일 01:00 일정 업데이트
    @Scheduled(cron = "0 0 1 * * *")
    public void updateDailyEvents() {
        log.info("Updating daily events information...");
        calendarService.getAllEvents();
    }

    // 1시간마다 google access token update
    @Scheduled(fixedRate = 3600000)
    public void refreshTokensPeriodically() {
        log.info("Refresh tokens periodically");
        tokenService.refreshTokens();
    }

    // 매일 05:00 날씨 업데이트
    @Scheduled(cron = "0 0 5 * * *")
    public void updateDailyWeather() {
        log.info("Updating daily weather information...");
        weatherService.getWeather();
    }
}
