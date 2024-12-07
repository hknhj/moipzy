package com.wrongweather.moipzy.domain.crawling.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CrawlingResponseDto {
    private String productName;
    private String imageUrl;

    @Builder
    public CrawlingResponseDto(String productName, String imageUrl) {
        this.productName = productName;
        this.imageUrl = imageUrl;
    }
}
