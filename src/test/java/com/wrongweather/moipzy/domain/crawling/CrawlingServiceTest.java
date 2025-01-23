package com.wrongweather.moipzy.domain.crawling;

import com.wrongweather.moipzy.domain.crawling.dto.CrawlingResponseDto;
import com.wrongweather.moipzy.domain.crawling.exception.CrawlingFailedException;
import com.wrongweather.moipzy.domain.crawling.service.CrawlingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CrawlingServiceTest {

    @InjectMocks
    private CrawlingService crawlingService;

    @Test
    @DisplayName("무신사 크롤링")
    void testCrawling() {
        // given
        String url = "https://www.musinsa.com/products/3640018";

        // when
        CrawlingResponseDto crawlingResponseDto = crawlingService.crawlMusinsa(url);

        // then
        assertEquals("COLOR BLOCK INSULATION YACHT PARKA NAVY", crawlingResponseDto.getProductName());
        assertNotNull(crawlingResponseDto.getImageUrl());
    }

    @Test
    @DisplayName("유효하지 않은 url 크롤링")
    void testInvalidUrlCrawling() {
        // given
        String url = "https://invalid.com";

        // when & then
        assertThatThrownBy(() -> crawlingService.crawlMusinsa(url))
                .isInstanceOf(CrawlingFailedException.class);
    }
}
