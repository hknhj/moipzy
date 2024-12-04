package com.wrongweather.moipzy.domain.kakao.service;

import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.email.service.EmailService;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import com.wrongweather.moipzy.domain.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class KaKaoService {

    private final StyleService styleService;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;

    // kakao id 빠른 조회를 위한 SET KEY
    private static final String USER_SET_KEY = "kakaoIds";
    // 이메일 유효성 검사 정규식
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    public Map<String, Object> getStyleRecommends(String utterance, String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        String userId = redisTemplate.opsForValue().get(kakaoId);

        long beforeTime = System.currentTimeMillis();

        //최저기온, 최고기온 순서, 하루에 한 번만 실행하고 꺼내쓰면 됨
        int minTemp = -100;
        int maxTemp = 100;
        String date = "오늘내일";
        String eventDate = "todaytomorrow";

        if (utterance.equals("오늘 옷 추천하기")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMinTemp"));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMaxTemp"));
            date = "오늘";
            eventDate = "today";
        } else if (utterance.equals("내일 옷 추천하기")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMinTemp"));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMaxTemp"));
            date = "내일";
            eventDate = "tomorrow";
        }

        log.info("minTemp: {}", minTemp);
        log.info("maxTemp: {}", maxTemp);
        log.info("successful");

        // 유저의 일정을 가져옴
        String events = (String) redisTemplate.opsForHash().get(userId, eventDate);
        log.info(eventDate + "events: {}", events);
        log.info("successful");

        //기온과 일정을 토대로 옷차림을 추천함
        List<StyleRecommendResponseDto> recommends = styleService.recommendTest(Integer.parseInt(userId), minTemp, maxTemp, events);

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

        // 기온 정보를 텍스트로 추가
        Map<String, Object> temperatureText = new HashMap<>();
        temperatureText.put("type", "simpleText");
        temperatureText.put("text", date + "의 기온은 최저 " + minTemp + "°C, 최고 " + maxTemp + "°C입니다.");
        outputs.add(temperatureText);

        // 일정 정보를 텍스트로 추가
        Map<String, Object> scheduleText = new HashMap<>();
        scheduleText.put("type", "simpleText");
        scheduleText.put("text", date + "의 일정은 다음과 같습니다: " + events);
        outputs.add(scheduleText);

        template.put("outputs", outputs);
        response.put("template", template);

        long afterTime = System.currentTimeMillis();

        long secDiffTime = (afterTime - beforeTime);

        log.info("secDiffTime: {}", secDiffTime);

        return response;
    }

    public Map<String, Object> getWeather(String userId) {
        if (!isUserAuthenticated(userId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // 오늘, 내일의 최저기온, 최고기온
        int TodayMinTemp = Integer.parseInt(valueOperations.get("todayMinTemp"));
        int TodayMaxTemp = Integer.parseInt(valueOperations.get("todayMaxTemp"));
        int TomorrowMinTemp = Integer.parseInt(valueOperations.get("tomorrowMinTemp"));
        int TomorrowMaxTemp = Integer.parseInt(valueOperations.get("tomorrowMaxTemp"));

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 날짜 출력 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        String todayTemperatureExplanation = formattedTodayDate + " 오늘의 기온은 최저 " + TodayMinTemp + "°C, 최고 " + TodayMaxTemp + "°C입니다.";
        String tomorrowTemperatureExplanation = formattedTomorrowDate + " 내일의 기온은 최저 " + TomorrowMinTemp + "°C, 최고 " + TomorrowMaxTemp + "°C입니다.";

        return createSimpleTextResponse(Arrays.asList(todayTemperatureExplanation, tomorrowTemperatureExplanation));
    }

    public Map<String, Object> getEvents(String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        String userId = redisTemplate.opsForValue().get("kakaoId:" + kakaoId);
        if (userId == null)
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        // 오늘, 내일 일정 불러오기
        String todayEvent = (String) redisTemplate.opsForHash().get(userId, "today");
        String tomorrowEvent = (String) redisTemplate.opsForHash().get(userId, "tomorrow");

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 날짜 출력 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        String todayEventExplanation = formattedTodayDate + " 오늘 일정: " + todayEvent;
        String tomorrowEventExplanation = formattedTomorrowDate + " 내일 일정: " + tomorrowEvent;

        return createSimpleTextResponse(Arrays.asList(todayEventExplanation, tomorrowEventExplanation));
    }

    public Map<String, Object> fallbackBlock(String utterance, String kakaoId) {
        if (isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("이미 등록된 유저입니다."));

        log.info("isAuthCode: {}", isAuthCode(utterance));

        if (Pattern.matches(EMAIL_REGEX, utterance)) { //폴백블록 이메일 입력했을 때

            String email = utterance;

            if (!userService.isRegistered(email))
                return createSimpleTextResponse(Arrays.asList("등록되지 않은 이메일입니다."));

            String verification = emailService.sendVerificationEmail(email); // 입력받은 이메일로 인증 메일을 발송
            redisTemplate.opsForHash().put(kakaoId, "email", email); // key: kakaoId / value: verification, email
            redisTemplate.opsForHash().put(kakaoId, "verification", verification);

            return createSimpleTextResponse(Arrays.asList("해당 이메일로 인증메일을 전송했습니다. 인증번호를 입력하세요."));

        } else if (isAuthCode(utterance)) { //폴백블록 인증번호 입력했을 때

            String verification = utterance;
            String email = (String) redisTemplate.opsForHash().get(kakaoId, "email"); //email 추출

            if (utterance.equals(redisTemplate.opsForHash().get(kakaoId, "verification"))) {
                redisTemplate.opsForSet().add("kakaoIds", kakaoId); // kakaoIds에 kakaoId 추가

                redisTemplate.delete(kakaoId); // kakaoId가 key인 인증 데이터 삭제
                User user = userRepository.findByEmail(email).get(); // repository를 직접적으로 이용해서 dirty checking이 가능해서 자동으로
                String userId = Integer.toString(user.getUserId()); // email로 userId 찾기
                user.updateKakaoId(kakaoId);
                userRepository.save(user); //kakao_id column 업데이트

                redisTemplate.opsForValue().set(kakaoId, userId); // key:kakaoId, value: userId
                return createSimpleTextResponse(Arrays.asList("등록되었습니다."));
            } else {
                return createSimpleTextResponse(Arrays.asList("인증번호가 올바르지 않습니다."));
            }
        } else {
            return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));
        }
    }


    public boolean isUserAuthenticated(String kakaoId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(USER_SET_KEY, kakaoId));
    }

    public boolean isAuthCode(String input) {
        // 인증번호는 4자리 숫자로만 구성된 문자열인지 확인
        return input != null && input.matches("\\d{4}");
    }

    // 출력하고자 하는 문장을 simpleText 형식의 JSON 구조를 생성
    public Map<String, Object> createSimpleTextResponse(List<String> messages) {
        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();

        // 메시지 리스트를 반복하며 simpleText 객체 생성
        for (String message : messages) {
            Map<String, Object> simpleText = new HashMap<>();
            simpleText.put("text", message); //text 내용 설정

            // simpleText를 포함한 outputs 항목 생성
            Map<String, Object> output = new HashMap<>();
            output.put("simpleText", simpleText);
            outputs.add(output);
        }

        template.put("outputs", outputs);
        response.put("template", template);

        return response;
    }

}
