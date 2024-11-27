package com.wrongweather.moipzy.domain.style.dto;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StyleResponseDto {
    private int styleId; // 스타일 ID

    private int outerId; // 아우터 ID
    private String outerImgPath; // 아우터 이미지 경로

    private int topId; // 상의 ID
    private String topImgPath; // 상의 이미지 경로

    private int bottomId; // 하의 ID
    private String bottomImgPath; // 하의 이미지 경로

    private int highTemp; // 최고 기온
    private int lowTemp; // 최저 기온

    @Builder
    public StyleResponseDto(int styleId, Cloth outer, Cloth top, Cloth bottom, int highTemp, int lowTemp) {
        this.styleId = styleId;

        if (outer != null) {
            this.outerId = outer.getClothId();
            this.outerImgPath = outer.getClothImg() != null ? outer.getClothImg().getImgUrl() : null;
        }

        if (top != null) {
            this.topId = top.getClothId();
            this.topImgPath = top.getClothImg() != null ? top.getClothImg().getImgUrl() : null;
        }

        if (bottom != null) {
            this.bottomId = bottom.getClothId();
            this.bottomImgPath = bottom.getClothImg() != null ? bottom.getClothImg().getImgUrl() : null;
        }

        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
    }
}
