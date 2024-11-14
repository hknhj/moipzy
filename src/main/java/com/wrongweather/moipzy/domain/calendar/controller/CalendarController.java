package com.wrongweather.moipzy.domain.calendar.controller;

import com.google.api.services.calendar.model.Event;
import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/moipzy/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/tomorrow-events")
    public List<String> getUpcomingEvents(@RequestParam("access_token") String accessToken) throws IOException, GeneralSecurityException {
        System.out.println("google calendar try");
        return calendarService.getTomorrowEvents(accessToken);
    }
}
