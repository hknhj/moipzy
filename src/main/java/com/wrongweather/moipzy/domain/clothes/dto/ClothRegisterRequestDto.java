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
public class ClothRegisterRequestDto {
    private int userId;
    private LargeCategory largeCategory;
    private SmallCategory smallCategory;
    private Color color;
    private Degree degree;

    @Builder
    public ClothRegisterRequestDto(int userId, LargeCategory largeCategory, SmallCategory smallCategory, Color color, Degree degree) {
        this.userId = userId;
        this.largeCategory = largeCategory;
        this.smallCategory = smallCategory;
        this.color = color;
        this.degree = degree;
    }

    public Cloth toEntity(User user, ClothImage clothImage) {
        return Cloth.builder()
                .user(user)
                .clothImage(clothImage)
                .largeCategory(largeCategory)
                .smallCategory(smallCategory)
                .color(color)
                .degree(degree)
                .build();
    }
}
