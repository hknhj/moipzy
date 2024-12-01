package com.wrongweather.moipzy.domain.kakao.service;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import com.wrongweather.moipzy.domain.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KaKaoService {

    private final StyleService styleService;
    private final WeatherService weatherService;
    private final CalendarService calendarService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> getStyleRecommends(String utterance) {
        int userId = 8;

        long beforeTime = System.currentTimeMillis();
        //utterance로 today, tomorrow 입력받음
        LocalDate localDate = getLocalDate(utterance);

        //최저기온, 최고기온 순서, 하루에 한 번만 실행하고 꺼내쓰면 됨

        // 유저의 일정을 가져옴
        String events = calendarService.getEvents(userId, localDate);

        //기온과 일정을 토대로 옷차림을 추천함
        List<StyleRecommendResponseDto> recommends = styleService.recommendTest(userId, 10, 5, events);

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();


        //recommend 각 요소의 url은 완전한 url임. 그대로 사용하면 됨
        for (StyleRecommendResponseDto recommend : recommends) {
            // 카로셀 아이템들 생성
            Map<String, Object> carousel = new HashMap<>();
            carousel.put("type", "basicCard");

            // 카로셀 아이템 목록
            List<Map<String, Object>> items = new ArrayList<>();

            // 아우터 아이템
            if (recommend.getOuterId() != 0) {
                Map<String, Object> outerItem = new HashMap<>();
                outerItem.put("title", "아우터");
                Color outerColor = recommend.getOuterColor();
                SmallCategory outerSmallCategory = recommend.getOuterSmallCategory();
                String outerDescription = outerColor.name() + outerSmallCategory.name();
                outerItem.put("description", outerDescription);
                Map<String, Object> outerThumbnail = new HashMap<>();
                outerThumbnail.put("imageUrl", recommend.getOuterImgPath());
                outerThumbnail.put("fixedRatio", true);
                outerItem.put("thumbnail", outerThumbnail);
                items.add(outerItem);
            }

            // 상의 아이템
            Map<String, Object> topItem = new HashMap<>();
            topItem.put("title", "상의");
            Color topColor = recommend.getTopColor();
            SmallCategory topSmallCategory = recommend.getTopSmallCategory();
            String topDescription = topColor.name() + topSmallCategory.name();
            topItem.put("description", topDescription);
            Map<String, Object> topThumbnail = new HashMap<>();
            topThumbnail.put("imageUrl", recommend.getTopImgPath());
            topThumbnail.put("fixedRatio", true);
            topItem.put("thumbnail", topThumbnail);
            items.add(topItem);

            // 하의 아이템
            Map<String, Object> bottomItem = new HashMap<>();
            bottomItem.put("title", "하의");
            Color bottomColor = recommend.getBottomColor();
            SmallCategory bottomSmallCategory = recommend.getBottomSmallCategory();
            String bottomDescription = bottomColor.name() + bottomSmallCategory.name();
            bottomItem.put("description", bottomDescription);
            Map<String, Object> bottomThumbnail = new HashMap<>();
            bottomThumbnail.put("imageUrl", recommend.getBottomImgPath());
            bottomThumbnail.put("fixedRatio", true);
            bottomItem.put("thumbnail", bottomThumbnail);
            items.add(bottomItem);

            carousel.put("items", items);
            outputs.add(Map.of("carousel", carousel));
        }

        template.put("outputs", outputs);
        response.put("template", template);

        long afterTime = System.currentTimeMillis();

        long secDiffTime = (afterTime - beforeTime);

        log.info("secDiffTime: {}", secDiffTime);

        return response;
    }

    public Map<String, Object> getStyleRecommendsTest(String utterance) {
        int userId = 8;
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        long beforeTime = System.currentTimeMillis();
        //utterance로 today, tomorrow 입력받음
        LocalDate localDate = getLocalDate(utterance);

        //최저기온, 최고기온 순서, 하루에 한 번만 실행하고 꺼내쓰면 됨
        int minTemp = (int)valueOperations.get("todayMinTemp");
        int maxTemp = (int)valueOperations.get("todayMaxTemp");

        log.info("minTemp: {}", minTemp);
        log.info("maxTemp: {}", maxTemp);
        log.info("successful");


        // 유저의 일정을 가져옴
        String todayEvents = (String)redisTemplate.opsForHash().get(Integer.toString(userId), "today");
        log.info("todayEvents: {}", todayEvents);
        log.info("successful");

        //기온과 일정을 토대로 옷차림을 추천함
        List<StyleRecommendResponseDto> recommends = styleService.recommendTest(userId, minTemp, maxTemp, todayEvents);

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();


        //recommend 각 요소의 url은 완전한 url임. 그대로 사용하면 됨
        for (StyleRecommendResponseDto recommend : recommends) {
            // 카로셀 아이템들 생성
            Map<String, Object> carousel = new HashMap<>();
            carousel.put("type", "basicCard");

            // 카로셀 아이템 목록
            List<Map<String, Object>> items = new ArrayList<>();

            // 아우터 아이템
            if (recommend.getOuterId() != 0) {
                Map<String, Object> outerItem = new HashMap<>();
                outerItem.put("title", "아우터");
                Color outerColor = recommend.getOuterColor();
                SmallCategory outerSmallCategory = recommend.getOuterSmallCategory();
                String outerDescription = outerColor.name() + " " + outerSmallCategory.name();
                outerItem.put("description", outerDescription);
                Map<String, Object> outerThumbnail = new HashMap<>();
                outerThumbnail.put("imageUrl", recommend.getOuterImgPath());
                outerThumbnail.put("fixedRatio", true);
                outerItem.put("thumbnail", outerThumbnail);
                items.add(outerItem);
            }

            // 상의 아이템
            Map<String, Object> topItem = new HashMap<>();
            topItem.put("title", "상의");
            Color topColor = recommend.getTopColor();
            SmallCategory topSmallCategory = recommend.getTopSmallCategory();
            String topDescription = topColor.name() + " " + topSmallCategory.name();
            topItem.put("description", topDescription);
            Map<String, Object> topThumbnail = new HashMap<>();
            topThumbnail.put("imageUrl", recommend.getTopImgPath());
            topThumbnail.put("fixedRatio", true);
            topItem.put("thumbnail", topThumbnail);
            items.add(topItem);

            // 하의 아이템
            Map<String, Object> bottomItem = new HashMap<>();
            bottomItem.put("title", "하의");
            Color bottomColor = recommend.getBottomColor();
            SmallCategory bottomSmallCategory = recommend.getBottomSmallCategory();
            String bottomDescription = bottomColor.name() + " " + bottomSmallCategory.name();
            bottomItem.put("description", bottomDescription);
            Map<String, Object> bottomThumbnail = new HashMap<>();
            bottomThumbnail.put("imageUrl", recommend.getBottomImgPath());
            bottomThumbnail.put("fixedRatio", true);
            bottomItem.put("thumbnail", bottomThumbnail);
            items.add(bottomItem);

            carousel.put("items", items);
            outputs.add(Map.of("carousel", carousel));
        }

        template.put("outputs", outputs);
        response.put("template", template);

        long afterTime = System.currentTimeMillis();

        long secDiffTime = (afterTime - beforeTime);

        log.info("secDiffTime: {}", secDiffTime);

        return response;
    }

    public LocalDate getLocalDate(String date) {
        if ("today".equalsIgnoreCase(date)) {
            return LocalDate.now();
        } else if ("tomorrow".equalsIgnoreCase(date)) {
            return LocalDate.now().plusDays(1);
        } else {
            throw new IllegalArgumentException("Invalid date input. Use 'today' or 'tomorrow'.");
        }
    }
}
