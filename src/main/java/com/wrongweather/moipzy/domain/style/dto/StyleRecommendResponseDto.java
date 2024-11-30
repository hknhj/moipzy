package com.wrongweather.moipzy.domain.style.dto;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StyleRecommendResponseDto {
    private int outerId; // 아우터 ID
    private Color outerColor;
    private SmallCategory outerSmallCategory;
    private String outerImgPath; // 아우터 이미지 경로

    private int topId; // 상의 ID
    private Color topColor;
    private SmallCategory topSmallCategory;
    private String topImgPath; // 상의 이미지 경로

    private int bottomId; // 하의 ID
    private Color bottomColor;
    private SmallCategory bottomSmallCategory;
    private String bottomImgPath; // 하의 이미지 경로

    private int highTemp; // 최고 기온
    private int lowTemp; // 최저 기온

    private String explanation;
    private String style;

    @Builder
    public StyleRecommendResponseDto(Cloth outer, Cloth top, Cloth bottom, int highTemp, int lowTemp, String explanation, String style) {
        String domain = "https://moipzy.shop";
        if (outer != null) {
            this.outerId = outer.getClothId();
            this.outerColor = outer.getColor();
            this.outerSmallCategory = outer.getSmallCategory();
            this.outerImgPath = outer.getClothImg() != null ? domain + outer.getClothImg().getImgUrl() : null;
        }

        if (top != null) {
            this.topId = top.getClothId();
            this.topColor = top.getColor();
            this.topSmallCategory = top.getSmallCategory();
            this.topImgPath = top.getClothImg() != null ? domain + top.getClothImg().getImgUrl() : null;
        }

        if (bottom != null) {
            this.bottomId = bottom.getClothId();
            this.bottomColor = bottom.getColor();
            this.bottomSmallCategory = bottom.getSmallCategory();
            this.bottomImgPath = bottom.getClothImg() != null ? domain + bottom.getClothImg().getImgUrl() : null;
        }

        this.highTemp = highTemp;
        this.lowTemp = lowTemp;

        this.explanation = explanation;
        this.style = style;
    }
}
