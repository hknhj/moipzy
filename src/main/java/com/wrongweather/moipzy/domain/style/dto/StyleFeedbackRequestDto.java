package com.wrongweather.moipzy.domain.style.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StyleFeedbackRequestDto {
    private int styleId; // 스타일 ID
    private int outerId; // 아우터 ID
    private int topId; // 상의 ID
    private int bottomId; // 하의 ID
    private Feedback feedback; // HOT, GOOD, COLD 중 하나

    @Builder
    public StyleFeedbackRequestDto(int styleId, int outerId, int topId, int bottomId, Feedback feedback) {
        this.styleId = styleId;
        this.outerId = outerId;
        this.topId = topId;
        this.bottomId = bottomId;
        this.feedback = feedback;
    }
}
