package com.wrongweather.moipzy.domain.clothes.dto;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.users.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClothUpdateRequestDto {
    private LargeCategory largeCategory;
    private SmallCategory smallCategory;
    private Color color;
    private Degree degree;

    @Builder
    public ClothUpdateRequestDto(LargeCategory largeCategory, SmallCategory smallCategory, Color color, Degree degree) {
        this.largeCategory = largeCategory;
        this.smallCategory = smallCategory;
        this.color = color;
        this.degree = degree;
    }
}
