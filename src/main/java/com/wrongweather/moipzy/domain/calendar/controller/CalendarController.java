package com.wrongweather.moipzy.domain.calendar.controller;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/moipzy/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/tomorrow-events")
    public Map<String, List<String>> getUpcomingEvents(@RequestParam("access_token") String accessToken) throws IOException, GeneralSecurityException {
        return calendarService.getTodayAndTomorrowEvents(accessToken);
    }
}
