package com.wrongweather.moipzy.domain.style.dto;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.style.Style;
import com.wrongweather.moipzy.domain.users.User;
import lombok.Getter;

@Getter
public class StyleUploadRequestDto {
    private int userId;
    private int outerId;
    private int semiOuterId;
    private int topId;
    private int bottomId;
    private int feelTemp;

    public Style toEntity(User user, Cloth outer, Cloth semiOuter, Cloth top, Cloth bottom) {
        return Style.builder()
                .user(user)
                .outer(outer)
                .semiOuter(semiOuter)
                .top(top)
                .bottom(bottom)
                .feelTemp(feelTemp)
                .build();
    }
}
