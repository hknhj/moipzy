package com.wrongweather.moipzy.domain.calendar.controller;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/moipzy/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/calendar/{userId}")
    public String getEvents(@PathVariable int userId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws IOException {
        return calendarService.getEvents(userId, date);
    }
}
