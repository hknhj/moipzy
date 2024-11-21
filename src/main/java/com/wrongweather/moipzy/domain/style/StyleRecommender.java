package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.temperature.OuterTempRange;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.TopTempRange;

import java.util.List;

public interface StyleRecommender {
    public List<StyleRecommendResponseDto> recommend(TemperatureRange range, int feelTemp);

    public List<List<Cloth>> recommendByHighLow(OuterTempRange outerTempRange, TopTempRange topTempRange, int highTemp, int lowTemp);
}
