package com.wrongweather.moipzy.domain.clothes.dto;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.users.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClothResponseDto {
    private int userId;
    private int clothId;
    private LargeCategory largeCategory;
    private SmallCategory smallCategory;
    private Color color;
    private Degree degree;
    private String imgUrl;

    @Builder
    public ClothResponseDto(User user, Cloth cloth) {
        this.userId = user.getUserId();
        this.clothId = cloth.getClothId();
        this.largeCategory = cloth.getLargeCategory();
        this.smallCategory = cloth.getSmallCategory();
        this.color = cloth.getColor();
        this.degree = cloth.getDegree();
        this.imgUrl = cloth.getClothImg().getImgUrl();
    }
}
