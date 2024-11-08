package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;

import java.util.List;

public interface StyleRecommender {
    public List<StyleRecommendResponseDto> recommend(TemperatureRange range, int feelTemp);
}
