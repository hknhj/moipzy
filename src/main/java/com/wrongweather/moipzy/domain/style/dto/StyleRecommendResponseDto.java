package com.wrongweather.moipzy.domain.style.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StyleRecommendResponseDto {
    private Integer outerId;
    private Integer topId;
    private Integer bottomId;

    @Builder
    public StyleRecommendResponseDto(Integer outerId, Integer topId, Integer bottomId) {
        this.outerId = outerId;
        this.topId = topId;
        this.bottomId = bottomId;
    }
}
