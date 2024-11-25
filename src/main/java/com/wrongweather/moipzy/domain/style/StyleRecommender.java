package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;

import java.util.List;

public interface StyleRecommender {
    public List<List<Cloth>> recommendByHighLowTemp(int highTemp, int lowTemp);
}
