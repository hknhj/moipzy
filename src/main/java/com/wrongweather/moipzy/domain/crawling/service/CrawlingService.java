package com.wrongweather.moipzy.domain.crawling.service;

import com.wrongweather.moipzy.domain.crawling.dto.CrawlingResponseDto;
import com.wrongweather.moipzy.domain.crawling.exception.CrawlingFailedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CrawlingService {

    public CrawlingResponseDto crawlMusinsa(String url) {
        try {
            // Jsoup으로 HTML 문서 가져오기
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10_000)
                    .get();

            // 이미지 URL 크롤링
            Element imageElement = doc.selectFirst("meta[property=og:image]");
            String imageUrl = imageElement != null ? imageElement.attr("content") : "이미지 URL 없음";

            // 상품명 크롤링
            Element productNameElement = doc.selectFirst("meta[property=og:title]");
            String rawTitle = productNameElement != null ? productNameElement.attr("content") : "상품명 없음";
            String productName = extractProductName(rawTitle);

            // 결과 출력
            System.out.println("이미지 URL: " + imageUrl);
            System.out.println("상품명: " + productName);

            return CrawlingResponseDto.builder()
                    .productName(productName)
                    .imageUrl(imageUrl)
                    .build();
        } catch (IOException e) {
            throw new CrawlingFailedException("크롤링 중 문제가 발생했습니다. URL: " + url, e);
        }
    }

    /**
     * 상품명과 색상만 추출하는 메서드
     */
    private static String extractProductName(String rawTitle) {
        // 1. 브랜드명 및 뒤의 "사이즈 & 후기 | 무신사" 제거
        String cleanedTitle = rawTitle.replaceAll("^.*?\\)\\s*", "") // 브랜드명 제거
                .replaceAll("\\s-\\s사이즈.*$", ""); // "사이즈 & 후기 | 무신사" 제거

        return cleanedTitle.trim(); // 앞뒤 공백 제거 후 반환
    }
}
