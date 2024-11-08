package com.wrongweather.moipzy.domain.temperature.service;

import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.TemperatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final TemperatureRepository temperatureRepository;

    /**
     * 유저가 생성되면 default temperature range 설정하는 함수
     * @return TemperatureRange
     */
    public TemperatureRange setDefaultRange() {
        TemperatureRange temperatureRange = TemperatureRange.builder()
                .over28("1,2")
                .between27_24("3,4")
                .between23_20("5,6,7")
                .between19_17("8,9,10")
                .between16_14("11,12,13")
                .between13_11("14,15")
                .between10_8("16,17,18")
                .between7_5("19,20,21")
                .under4("22,23,24").build();

        return temperatureRepository.save(temperatureRange);
    }
}
