package com.wrongweather.moipzy.domain.style.dto;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.style.Style;
import com.wrongweather.moipzy.domain.users.User;
import lombok.Getter;

@Getter
public class StyleUploadRequestDto {
    private Integer userId;
    private Integer outerId;
    private Integer topId;
    private Integer bottomId;
    private Integer feelTemp;

    public Style toEntity(User user, Cloth outer, Cloth top, Cloth bottom) {
        return Style.builder()
                .user(user)
                .outer(outer)
                .top(top)
                .bottom(bottom)
                .feelTemp(feelTemp)
                .build();
    }
}
