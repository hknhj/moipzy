package com.wrongweather.moipzy.domain.kakao.service;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.email.service.EmailService;
import com.wrongweather.moipzy.domain.style.Style;
import com.wrongweather.moipzy.domain.style.StyleRepository;
import com.wrongweather.moipzy.domain.style.dto.Feedback;
import com.wrongweather.moipzy.domain.style.dto.StyleFeedbackRequestDto;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.dto.StyleUploadRequestDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import com.wrongweather.moipzy.domain.token.TokenRepository;
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
    private final StyleRepository styleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ClothRepository clothRepository;

    private static final String USER_SET_KEY = "kakaoIds";
    private static final String TODAY_MIN_TEMP = "todayMinTemp";
    private static final String TODAY_MAX_TEMP = "todayMaxTemp";
    private static final String TOMORROW_MIN_TEMP = "tomorrowMinTemp";
    private static final String TOMORROW_MAX_TEMP = "tomorrowMaxTemp";

    // 이메일 유효성 검사 정규식
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    // n번 유효성 검사 정규식
    private static final String SELECT_REGEX = "^(오늘|내일)\\s*+(\\d+)번$";
    // img url
    private static final String IMG_URL = "https://moipzy.shop";

    public Map<String, Object> getStyleRecommendsInRedis(String utterance, String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        String koreanDate = "";

        List<String> clothIds = new ArrayList<>();

        if (utterance.equals("오늘 옷 추천하기")) {
            for (int i=1; i<=3; i++)
                clothIds.add((String) redisTemplate.opsForHash().get(kakaoId, formattedTodayDate + "Recommend" + i));
            koreanDate = "오늘";
        } else if (utterance.equals("내일 옷 추천하기")) {
            for (int i=1; i<=3; i++)
                clothIds.add((String) redisTemplate.opsForHash().get(kakaoId, formattedTomorrowDate + "Recommend" + i));
            koreanDate = "내일";
        }

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();

        for (String clothId : clothIds) {
            // 문자열을 쉼표로 분리
            String[] stringNumbers = clothId.split(",");

            int[] numbers = Arrays.stream(stringNumbers)
                    .mapToInt(Integer::parseInt)
                    .toArray();

            // Cloth 객체 초기화
            Cloth outer = null;
            Cloth top = null;
            Cloth bottom = null;

            // 정수 배열의 길이에 따라 처리
            if (numbers.length == 3) {
                // 아우터, 상의, 하의 모두 있는 경우
                outer = clothRepository.findByClothId(numbers[0]).orElse(null);
                top = clothRepository.findByClothId(numbers[1]).orElse(null);
                bottom = clothRepository.findByClothId(numbers[2]).orElse(null);
            } else if (numbers.length == 2) {
                // 상의와 하의만 있는 경우
                top = clothRepository.findByClothId(numbers[0]).orElse(null);
                bottom = clothRepository.findByClothId(numbers[1]).orElse(null);
            } else {
                throw new IllegalArgumentException("Invalid number of clothing IDs provided.");
            }

            // 카로셀 아이템들 생성
            Map<String, Object> carousel = new HashMap<>();
            carousel.put("type", "basicCard");

            // 카로셀 아이템 목록
            List<Map<String, Object>> items = new ArrayList<>();

            // 아우터 아이템
            if (outer != null) {
                Map<String, Object> outerItem = new HashMap<>();
                outerItem.put("title", "아우터");
                Color outerColor = outer.getColor();
                SmallCategory outerSmallCategory = outer.getSmallCategory();
                String outerDescription = outerColor.name() + " " + outerSmallCategory.name();
                outerItem.put("description", outerDescription);
                Map<String, Object> outerThumbnail = new HashMap<>();
                outerThumbnail.put("imageUrl", IMG_URL + outer.getClothImg().getImgUrl());
                outerThumbnail.put("fixedRatio", true);
                outerItem.put("thumbnail", outerThumbnail);
                items.add(outerItem);
            }

            // 상의 아이템
            if (top == null)
                break;
            Map<String, Object> topItem = new HashMap<>();
            topItem.put("title", "상의");
            Color topColor = top.getColor();
            SmallCategory topSmallCategory = top.getSmallCategory();
            String topDescription = topColor.name() + " " + topSmallCategory.name();
            topItem.put("description", topDescription);
            Map<String, Object> topThumbnail = new HashMap<>();
            topThumbnail.put("imageUrl", IMG_URL + top.getClothImg().getImgUrl());
            topThumbnail.put("fixedRatio", true);
            topItem.put("thumbnail", topThumbnail);
            items.add(topItem);

            // 하의 아이템
            if (bottom == null)
                break;
            Map<String, Object> bottomItem = new HashMap<>();
            bottomItem.put("title", "하의");
            Color bottomColor = bottom.getColor();
            SmallCategory bottomSmallCategory = bottom.getSmallCategory();
            String bottomDescription = bottomColor.name() + " " + bottomSmallCategory.name();
            bottomItem.put("description", bottomDescription);
            Map<String, Object> bottomThumbnail = new HashMap<>();
            bottomThumbnail.put("imageUrl", IMG_URL + bottom.getClothImg().getImgUrl());
            bottomThumbnail.put("fixedRatio", true);
            bottomItem.put("thumbnail", bottomThumbnail);
            items.add(bottomItem);

            carousel.put("items", items);
            outputs.add(Map.of("carousel", carousel));
        }

        //quickReplies
        List<Map<String, Object>> quickReplies = new ArrayList<>();
        for (int i = 0; i < clothIds.size(); i++) {
            Map<String, Object> quickReply = new HashMap<>();
            quickReply.put("messageText", koreanDate + " " + (i + 1) + "번"); //오늘 1번
            quickReply.put("action", "message");
            quickReply.put("label", (i + 1) + "번");
            quickReplies.add(quickReply);
        }

        template.put("outputs", outputs);
        template.put("quickReplies", quickReplies);

        response.put("template", template);

        return response;
    }

    public Map<String, Object> getStyleRecommends(String utterance, String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");

        long beforeTime = System.currentTimeMillis();

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        //최저기온, 최고기온 순서, 하루에 한 번만 실행하고 꺼내쓰면 됨
        int minTemp = -100;
        int maxTemp = 100;
        String date = "todaytomorrow";
        String eventDate = "";
        String koreanDate = "";

        if (utterance.equals("오늘 옷 추천하기")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get(TODAY_MIN_TEMP));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get(TODAY_MAX_TEMP));
            date = "today";
            eventDate = formattedTodayDate;
            koreanDate = "오늘";
        } else if (utterance.equals("내일 옷 추천하기")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get(TOMORROW_MIN_TEMP));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get(TOMORROW_MAX_TEMP));
            date = "tomorrow";
            eventDate = formattedTomorrowDate;
            koreanDate = "내일";
        }

        log.info("minTemp: {}", minTemp);
        log.info("maxTemp: {}", maxTemp);
        log.info("successful");

        // 유저의 일정을 가져옴
        String events = (String) redisTemplate.opsForHash().get(userId, date);
        log.info(eventDate + "events: {}", events);
        log.info("successful");

        //기온과 일정을 토대로 옷차림을 추천함
        List<StyleRecommendResponseDto> recommends = styleService.recommend(Integer.parseInt(userId), maxTemp, minTemp, events);

        List<String> clothIds = new ArrayList<>();

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();

        //recommend 각 요소의 url은 완전한 url임. 그대로 사용하면 됨
        for (StyleRecommendResponseDto recommend : recommends) {

            // 옷 조합의 id들을 저장하기 위한 문자열
            String ids = "";

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
                ids += recommend.getOuterId() + ",";
            }

            // 상의 아이템
            if (recommend.getTopId() == 0)
                break;
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
            ids += recommend.getTopId() + ",";

            // 하의 아이템
            if (recommend.getBottomId() == 0)
                break;
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
            ids += recommend.getBottomId();

            carousel.put("items", items);
            outputs.add(Map.of("carousel", carousel));
            clothIds.add(ids);
        }

        //quickReplies
        List<Map<String, Object>> quickReplies = new ArrayList<>();
        for (int i = 0; i < clothIds.size(); i++) {
            Map<String, Object> quickReply = new HashMap<>();
            quickReply.put("messageText", koreanDate + " " + (i + 1) + "번"); //오늘 1번
            quickReply.put("action", "message");
            quickReply.put("label", (i + 1) + "번");
            quickReplies.add(quickReply);
        }

        template.put("outputs", outputs);
        template.put("quickReplies", quickReplies);

        response.put("template", template);

        // 똑같은 곳에 쓰면 덮어씌워진다
        // todayRecommend, tomorrowRecommend
        // 덮는 대신 지우고 다시 쓴다
        for (int i = 1; i <= 3; i++)
            redisTemplate.opsForHash().delete(kakaoId, eventDate + "Recommend" + i); //24-12-05Recommend1

        int i = 1;
        for (String clothId : clothIds) {
            redisTemplate.opsForHash().put(kakaoId, eventDate + "Recommend" + i, clothId);
            log.info(eventDate + "Recommend" + i + ": {}", redisTemplate.opsForHash().get(kakaoId, eventDate + "Recommend" + i)); //24-12-05Recommend2
            i++;
        }

        long afterTime = System.currentTimeMillis();

        log.info("secDiffTime: {}", afterTime - beforeTime);

        return response;
    }

    public Map<String, Object> getWeather(String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        String todayTemperatureExplanation = "(" + formattedTodayDate + ")" + " 오늘의 기온은 \n최저 " + TodayMinTemp + "°C, 최고 " + TodayMaxTemp + "°C입니다.";
        String tomorrowTemperatureExplanation = "(" + formattedTomorrowDate + ")" + " 내일의 기온은 \n최저 " + TomorrowMinTemp + "°C, 최고 " + TomorrowMaxTemp + "°C입니다.";

        return createSimpleTextResponse(Arrays.asList(todayTemperatureExplanation, tomorrowTemperatureExplanation));
    }

    public Map<String, Object> getEvents(String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");
        if (userId == null)
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        if (!tokenRepository.existsByUserId(Integer.parseInt(userId)))
            return createSimpleTextResponse(Arrays.asList("구글 캘린더와 연동되지 않은 계정입니다."));

        // 오늘, 내일 일정 불러오기
        String todayEvent = (String) redisTemplate.opsForHash().get(userId, "today");
        String tomorrowEvent = (String) redisTemplate.opsForHash().get(userId, "tomorrow");

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        String todayEventExplanation = "(" + formattedTodayDate + ")" + " 오늘 일정: ";
        if (todayEvent == null) {
            todayEventExplanation += "없음";
        } else {
            todayEventExplanation += "\n\n" + todayEvent;
        }
        String tomorrowEventExplanation = "(" + formattedTomorrowDate + ")" + " 내일 일정: ";
        if (tomorrowEvent == null) {
            tomorrowEventExplanation += "없음";
        } else {
            tomorrowEventExplanation += "\n\n" + tomorrowEvent;
        }

        return createSimpleTextResponse(Arrays.asList(todayEventExplanation, tomorrowEventExplanation));
    }

    public Map<String, Object> getInformation(String utterance, String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        int minTemp = -100;
        int maxTemp = 100;
        String event = "";
        String information = "";

        String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        if (utterance.equals("오늘 정보")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMinTemp"));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMaxTemp"));
            event = (String) redisTemplate.opsForHash().get(userId, "today");
            information = "(" + formattedTodayDate + ")\n\n" + "(기온)\n- 최저기온 " + minTemp + "°C, 최고기온 " + maxTemp + "°C" + "\n\n";
            if (event == null) {
                information += "- 일정 없음";
            } else {
                information += "(일정)\n" + event;
            }
        } else if (utterance.equals("내일 정보")) {
            minTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMinTemp"));
            maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMaxTemp"));
            event = (String) redisTemplate.opsForHash().get(userId, "tomorrow");
            information = "(" + formattedTomorrowDate + ")\n\n" + "(기온)\n- 최저기온 " + minTemp + "°C, 최고 " + maxTemp + "°C" + "\n\n";
            if (event == null) {
                information += "- 일정 없음";
            } else {
                information += "(일정)\n" + event;
            }
        }
        return createSimpleTextResponse(Arrays.asList(information));
    }

    public Map<String, Object> getOutfit(String utterance, String kakaoId) {
        if (!isUserAuthenticated(kakaoId))
            return createSimpleTextResponse(Arrays.asList("등록되지 않은 유저입니다."));

        String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        String koreanDate = "";

        Style foundStyle = null;

        if (utterance.equals("오늘 옷차림")) {

            foundStyle = styleRepository.findByUser_UserIdAndWearAt(Integer.parseInt(userId), today).orElse(null);
            koreanDate = "오늘";
        } else if (utterance.equals("어제 옷차림")) {
            foundStyle = styleRepository.findByUser_UserIdAndWearAt(Integer.parseInt(userId), yesterday).orElse(null);
            koreanDate = "어제";
        }

        if (foundStyle == null)
            return createSimpleTextResponse(Arrays.asList("등록된 옷차림이 없습니다."));

        Cloth outer = clothRepository.findByClothId(foundStyle.getOuter().getClothId()).orElse(null);
        Cloth top = clothRepository.findByClothId(foundStyle.getTop().getClothId()).orElse(null);
        Cloth bottom = clothRepository.findByClothId(foundStyle.getBottom().getClothId()).orElse(null);

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();

        // 카로셀 아이템들 생성
        Map<String, Object> carousel = new HashMap<>();
        carousel.put("type", "basicCard");

        // 카로셀 아이템 목록
        List<Map<String, Object>> items = new ArrayList<>();

        // full url
        String fullurl = "https://moipzy.shop";

        // 아우터 아이템
        if (outer != null) {
            Map<String, Object> outerItem = new HashMap<>();
            outerItem.put("title", "아우터");
            Color outerColor = outer.getColor();
            SmallCategory outerSmallCategory = outer.getSmallCategory();
            String outerDescription = outerColor.name() + " " + outerSmallCategory.name();
            outerItem.put("description", outerDescription);
            Map<String, Object> outerThumbnail = new HashMap<>();
            outerThumbnail.put("imageUrl", fullurl + outer.getClothImg().getImgUrl());
            outerThumbnail.put("fixedRatio", true);
            outerItem.put("thumbnail", outerThumbnail);
            items.add(outerItem);
        }

        // 상의 아이템
        if (top != null) {
            Map<String, Object> topItem = new HashMap<>();
            topItem.put("title", "상의");
            Color topColor = top.getColor();
            SmallCategory topSmallCategory = top.getSmallCategory();
            String topDescription = topColor.name() + " " + topSmallCategory.name();
            topItem.put("description", topDescription);
            Map<String, Object> topThumbnail = new HashMap<>();
            topThumbnail.put("imageUrl", fullurl + top.getClothImg().getImgUrl());
            topThumbnail.put("fixedRatio", true);
            topItem.put("thumbnail", topThumbnail);
            items.add(topItem);
        }

        // 하의 아이템
        if (bottom != null) {
            Map<String, Object> bottomItem = new HashMap<>();
            bottomItem.put("title", "하의");
            Color bottomColor = bottom.getColor();
            SmallCategory bottomSmallCategory = bottom.getSmallCategory();
            String bottomDescription = bottomColor.name() + " " + bottomSmallCategory.name();
            bottomItem.put("description", bottomDescription);
            Map<String, Object> bottomThumbnail = new HashMap<>();
            bottomThumbnail.put("imageUrl", fullurl + bottom.getClothImg().getImgUrl());
            bottomThumbnail.put("fixedRatio", true);
            bottomItem.put("thumbnail", bottomThumbnail);
            items.add(bottomItem);
        }

        carousel.put("items", items);
        outputs.add(Map.of("carousel", carousel));

        //quickReplies
        List<Map<String, Object>> quickReplies = new ArrayList<>();
        quickReplies.add(Map.of(
                "messageText", koreanDate + " 더움",
                "action", "message",
                "label", "더움"
        ));
        quickReplies.add(Map.of(
                "messageText", koreanDate + " 만족",
                "action", "message",
                "label", "만족"
        ));
        quickReplies.add(Map.of(
                "messageText", koreanDate + " 추움",
                "action", "message",
                "label", "추움"
        ));

        template.put("outputs", outputs);
        template.put("quickReplies", quickReplies);

        response.put("template", template);

        return response;
    }

    public Map<String, Object> fallbackBlock(String utterance, String kakaoId) {

        if (isEmail(utterance)) { //폴백블록 이메일 입력했을 때

            String email = utterance;

            if (!userService.isRegistered(email))
                return createSimpleTextResponse(Arrays.asList("서비스에 등록되어있지 않은 이메일입니다."));

            sendVerificationEmail(email, kakaoId);

            return createSimpleTextResponse(Arrays.asList("해당 이메일로 인증메일을 전송했습니다. 인증번호를 입력하세요."));

        } else if (isAuthCode(utterance)) { //폴백블록 인증번호 입력했을 때

            String email = (String) redisTemplate.opsForHash().get(kakaoId, "email"); //email 추출

            if (utterance.equals(redisTemplate.opsForHash().get(kakaoId, "verification"))) {
                updateKakaoId(email, kakaoId);
                return createSimpleTextResponse(Arrays.asList("등록되었습니다."));
            } else {
                return createSimpleTextResponse(Arrays.asList("인증번호가 올바르지 않습니다."));
            }

        } else if (isSelectNumber(utterance)) { // 옷추천 밑에 (오늘/내일) (1번/2번/3번) quickReplies를 이용해서 연동

            String noSpace = utterance.replace(" ", "");

            if (noSpace.length() != 4)
                return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));

            String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");

            String koreanDate = noSpace.substring(0, 2);
            String number = noSpace.substring(2, 3);

            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedTodayDate = today.format(formatter);
            String formattedTomorrowDate = tomorrow.format(formatter);
            LocalDate styleDate = LocalDate.now();

            int minTemp = -100;
            int maxTemp = 100;

            Style foundStyle = null;
            String style = "";

            if (koreanDate.equals("오늘")) {
                minTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMinTemp"));
                maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMaxTemp"));
                styleDate = today;
                foundStyle = styleRepository.findByUser_UserIdAndWearAt(Integer.parseInt(userId), today).orElse(null);
                style = (String) redisTemplate.opsForHash().get(kakaoId, formattedTodayDate + "Recommend" + number);
            } else if (koreanDate.equals("내일")) {
                minTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMinTemp"));
                maxTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMaxTemp"));
                styleDate = tomorrow;
                foundStyle = styleRepository.findByUser_UserIdAndWearAt(Integer.parseInt(userId), tomorrow).orElse(null);
                style = (String) redisTemplate.opsForHash().get(kakaoId, formattedTomorrowDate + "Recommend" + number);
            }

            int outerId = 0;
            int topId = 0;
            int bottomId = 0;

            String[] numbers = style.split(",");

            if (numbers.length == 3) {
                outerId = Integer.parseInt(numbers[0]);
                topId = Integer.parseInt(numbers[1]);
                bottomId = Integer.parseInt(numbers[2]);
            } else if (numbers.length == 2) {
                topId = Integer.parseInt(numbers[0]);
                outerId = Integer.parseInt(numbers[1]);
            }

            // 옷차림 db에 저장
            // style을 조회해서, 존재하면 수정, 없으면 추가
            if (foundStyle != null) {
                if (foundStyle.getFeedback() != null) {
                    createSimpleTextResponse(Arrays.asList("이미 옷차림이 등록되었습니다."));
                    log.info("userId: {}, style is already got feedback.", userId);
                }
                log.info("userId: {}, wearAt: {}, style isPresent", userId, formattedTodayDate);

                Cloth outer = clothRepository.findByClothId(outerId).orElse(null);
                Cloth top = clothRepository.findByClothId(topId).orElse(null);
                Cloth bottom = clothRepository.findByClothId(bottomId).orElse(null);

                foundStyle.updateStyle(outer, top, bottom);
                styleRepository.save(foundStyle);
            } else {
                log.info("userId: {}, wearAt: {}, style is not Present", userId, formattedTomorrowDate);


                styleService.uploadStyle(StyleUploadRequestDto.builder()
                        .outerId(outerId)
                        .topId(topId)
                        .bottomId(bottomId)
                        .wearAt(styleDate)
                        .highTemp(maxTemp)
                        .lowTemp(minTemp)
                        .userId(Integer.parseInt(userId))
                        .build());
            }

            return createSimpleTextResponse(Arrays.asList(number + "번 옷차림이 등록되었습니다."));

        } else if (utterance.contains("더움") || utterance.contains("만족") || utterance.contains("추움")) { // 옷차림 보여주고 (오늘/내일)+(더움/만족/추움) quickReplies 사용

            String noSpace = utterance.replaceAll(" ", "");

            if (noSpace.length() != 4)
                return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));

            String date = noSpace.substring(0, 2);
            if (!date.equals("오늘") && !date.equals("어제"))
                return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));

            String userId = (String) redisTemplate.opsForHash().get(kakaoId, "userId");

            String feedback = noSpace.substring(2, 4);

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedTodayDate = today.format(formatter);
            String formattedYesterdayDate = yesterday.format(formatter);
            LocalDate styleDate = LocalDate.now();

            String style = "";
            int outerId = 0;
            int topId = 0;
            int bottomId = 0;

            Optional<Style> foundStyle = styleRepository.findByUser_UserIdAndWearAt(Integer.parseInt(userId), styleDate);

            if (date.equals("오늘")) {
                style = (String) redisTemplate.opsForHash().get(kakaoId, formattedTodayDate + "Style");
            } else if (date.equals("어제")) {
                style = (String) redisTemplate.opsForHash().get(kakaoId, formattedYesterdayDate + "Style");
            }

            if (style.isEmpty())
                return createSimpleTextResponse(Arrays.asList("등록된 옷차림이 없습니다."));

            String[] idArray = style.split(",");

            if (idArray.length == 3) {
                outerId = Integer.parseInt(idArray[0]);
                topId = Integer.parseInt(idArray[1]);
                bottomId = Integer.parseInt(idArray[2]);
            } else if (idArray.length == 2) {
                topId = Integer.parseInt(idArray[0]);
                bottomId = Integer.parseInt(idArray[1]);
            }

            // 입었던 날짜를 토대로 옷차림 가져옴

            if (foundStyle.isEmpty()) {
                return createSimpleTextResponse(Arrays.asList("등록된 옷차림이 없습니다."));
            } else {
                int styleId = foundStyle.get().getStyleId();

                if (foundStyle.get().getFeedback()==null) {
                    switch (feedback) {
                        case "더움":
                            styleService.updateTemperature(StyleFeedbackRequestDto.builder()
                                    .styleId(styleId)
                                    .outerId(outerId)
                                    .topId(topId)
                                    .bottomId(bottomId)
                                    .feedback(Feedback.HOT)
                                    .build());
                            return createSimpleTextResponse(Arrays.asList("피드백이 적용 됐습니다."));
                        case "만족":
                            styleService.updateTemperature(StyleFeedbackRequestDto.builder()
                                    .styleId(styleId)
                                    .outerId(outerId)
                                    .topId(topId)
                                    .bottomId(bottomId)
                                    .feedback(Feedback.GOOD)
                                    .build());
                            return createSimpleTextResponse(Arrays.asList("피드백이 적용 됐습니다."));
                        case "추움":
                            styleService.updateTemperature(StyleFeedbackRequestDto.builder()
                                    .styleId(styleId)
                                    .outerId(outerId)
                                    .topId(topId)
                                    .bottomId(bottomId)
                                    .feedback(Feedback.COLD)
                                    .build());
                            return createSimpleTextResponse(Arrays.asList("피드백이 적용 됐습니다."));
                        default:
                            return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));
                    }
                } else {
                    return createSimpleTextResponse(Arrays.asList("이미 피드백이 완료됐습니다."));
                }
            }

        } else {
            return createSimpleTextResponse(Arrays.asList("잘못된 입력입니다."));
        }
    }

    private void sendVerificationEmail(String email, String kakaoId) {
        String verification = emailService.sendVerificationEmail(email); // 입력받은 이메일로 인증 메일을 발송
        redisTemplate.opsForHash().put(kakaoId, "email", email); // key: kakaoId / value: verification, email
        redisTemplate.opsForHash().put(kakaoId, "verification", verification);
    }

    private void updateKakaoId(String email, String kakaoId) {
        redisTemplate.opsForSet().add(USER_SET_KEY, kakaoId); // kakaoIds에 kakaoId 추가

        redisTemplate.delete(kakaoId); // kakaoId가 key인 인증 데이터 삭제
        User user = userRepository.findByEmail(email).get(); // repository를 직접적으로 이용해서 dirty checking이 가능해서 자동으로
        String userId = Integer.toString(user.getUserId()); // email로 userId 찾기
        user.updateKakaoId(kakaoId);
        userRepository.save(user); //kakao_id column 업데이트

        redisTemplate.opsForHash().put(kakaoId, "userId", userId); // key:kakaoId, value: userId
    }

    // 인증된 유저인지 확인
    private boolean isUserAuthenticated(String kakaoId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(USER_SET_KEY, kakaoId));
    }

    // 인증번호 형식인지 확인
    private boolean isAuthCode(String input) {
        // 인증번호는 4자리 숫자로만 구성된 문자열인지 확인
        return input != null && input.matches("\\d{4}");
    }

    // 이메일 형식인지 확인
    private boolean isEmail(String input) {
        return Pattern.matches(EMAIL_REGEX, input);
    }

    // n번 형식인지 확인
    private boolean isSelectNumber(String input) {
        return Pattern.matches(SELECT_REGEX, input);
    }

    // 출력하고자 하는 문장을 simpleText 형식의 JSON 구조를 생성
    private Map<String, Object> createSimpleTextResponse(List<String> messages) {
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
