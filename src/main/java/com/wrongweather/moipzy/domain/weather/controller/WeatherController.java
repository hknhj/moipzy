package com.wrongweather.moipzy.domain.weather.controller;

import com.wrongweather.moipzy.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public List<Integer> getWeather() {
        return weatherService.getWeather();
    }
}

