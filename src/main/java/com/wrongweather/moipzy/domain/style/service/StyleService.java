package com.wrongweather.moipzy.domain.style.service;

import com.wrongweather.moipzy.domain.chatGPT.dto.OutfitResponse;
import com.wrongweather.moipzy.domain.chatGPT.service.ChatGPTService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.style.Style;
import com.wrongweather.moipzy.domain.style.StyleRepository;
import com.wrongweather.moipzy.domain.style.dto.*;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StyleService {

    private final ClothRepository clothRepository;
    private final StyleRepository styleRepository;
    private final UserRepository userRepository;
    private final ChatGPTService chatGPTService;
    private final RedisTemplate<String, String> redisTemplate;

    private final int INF_HIGH_TEMPERATURE = 70;
    private final int INF_LOW_TEMPERATURE = -70;

    public void getAllStyles() {
        log.info("gellAllStyles");
        List<Object[]> results = userRepository.findUserAndKakaoIdForAllWithKakaoId();

        // 오늘, 내일 날짜 설정
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedTodayDate = today.format(formatter);
        String formattedTomorrowDate = tomorrow.format(formatter);

        int todayLowTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMinTemp"));
        int todayHighTemp = Integer.parseInt(redisTemplate.opsForValue().get("todayMaxTemp"));
        int tomorrowLowTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMinTemp"));
        int tomorrowHighTemp = Integer.parseInt(redisTemplate.opsForValue().get("tomorrowMaxTemp"));

        for (Object[] result : results) {
            int userId = (int) result[0]; // 수정: 캐스팅을 int로 변경
            String kakaoId = (String) result[1];

            String todayEvent = (String) redisTemplate.opsForHash().get(Integer.toString(userId), "today");
            String tomorrowEvent = (String) redisTemplate.opsForHash().get(Integer.toString(userId), "tomorrow");

            int i = 1;
            List<StyleRecommendResponseDto> todayRecommends = recommend(userId, todayHighTemp, todayLowTemp, todayEvent);
            for (StyleRecommendResponseDto todayRecommend : todayRecommends) {
                String clothId = todayRecommend.getOuterId() + "," + todayRecommend.getTopId() + "," + todayRecommend.getBottomId();
                redisTemplate.opsForHash().put(kakaoId, formattedTodayDate + "Recommend" + i, clothId);
                log.info("kakaoId: {}, todayRecommend{} : {}", kakaoId, i, clothId);
                i++;
            }

            i = 1;
            List<StyleRecommendResponseDto> tomorrowRecommends = recommend(userId, tomorrowHighTemp, tomorrowLowTemp, tomorrowEvent);
            for (StyleRecommendResponseDto tomorrowRecommend : tomorrowRecommends) {
                String clothId = tomorrowRecommend.getOuterId() + "," + tomorrowRecommend.getTopId() + "," + tomorrowRecommend.getBottomId();
                redisTemplate.opsForHash().put(kakaoId, formattedTomorrowDate + "Recommend" + i, clothId);
                log.info("kakaoId: {}, tomorrowRecommend{} : {}", kakaoId, i, clothId);
                i++;
            }
        }
    }

    public List<StyleRecommendResponseDto> recommend(int userId, int highTemp, int lowTemp, String events) {
        String prompt = "High temperature: "+ highTemp
                + ", Low temperature: "+ lowTemp + "\n";

        prompt += events;

        // 최고기온, 최저기온에 따라 아우터, 상의, 하의 리스트 추출
        List<Cloth> outer = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.OUTER, lowTemp, userId); //조회 결과가 없으면 빈 리스트를 반환한다. null 아님.
        List<Cloth> top = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.TOP, highTemp, userId);
        List<Cloth> bottom = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.BOTTOM, (highTemp+lowTemp)/2, userId);

        log.info("outerList: {}", outer);

        List<List<Cloth>> clothList = Arrays.asList(outer, top, bottom);

        // 모든 옷을 toString 메서드를 사용해 옷의 정보를 prompt에 넣는다.
        for (List<Cloth> clothes : clothList) {
            for (Cloth cloth : clothes) {
                prompt += cloth.toString() + ' ';
            }
        }

        OutfitResponse outfitResponse = chatGPTService.getChatGPTResponse(prompt);

        List<OutfitResponse.Response> outfits = outfitResponse.getOutfits();

        List<StyleRecommendResponseDto> recommends = new ArrayList<>();

        for (OutfitResponse.Response outfit : outfits) {
            Cloth recommendedOuter = clothRepository.findByClothId(outfit.getCombination().getOuter()).orElse(null);
            Cloth recommendedTop = clothRepository.findByClothId(outfit.getCombination().getTop()).orElse(null);
            Cloth recommendedBottom = clothRepository.findByClothId(outfit.getCombination().getBottom()).orElse(null);
            String explanation = outfit.getExplanation();
            String style = outfit.getStyle();

            recommends.add(StyleRecommendResponseDto.builder()
                    .outer(recommendedOuter)
                    .top(recommendedTop)
                    .bottom(recommendedBottom)
                    .highTemp(highTemp)
                    .lowTemp(lowTemp)
                    .explanation(explanation)
                    .style(style)
                    .build());
        }
        return recommends;
    }

    @Transactional
    public int uploadStyle(StyleUploadRequestDto styleUploadRequestDto) {

        User user = userRepository.findByUserId(styleUploadRequestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Style> foundStyle = styleRepository.findByUser_UserIdAndWearAt(user.getUserId(), styleUploadRequestDto.getWearAt());

        // 각 부위 Cloth entity or null 추출
        Cloth outer = findClothById(styleUploadRequestDto.getOuterId());
        Cloth top = findClothById(styleUploadRequestDto.getTopId());
        Cloth bottom = findClothById(styleUploadRequestDto.getBottomId());

        // 해당 유저의 해당 날짜에 옷차림이 존재하면 최근것으로 수정
        if (foundStyle.isPresent()) {
            Style existingStyle = foundStyle.get();
            if (existingStyle.getFeedback() != null) // 피드백이 완료됐으면 변경x
                return 0;

            existingStyle.updateStyle(outer, top, bottom);
            return styleRepository.save(existingStyle).getStyleId();
        }

        // 해당 유저의 해당 날짜에 옷차림이 존재하지 않으면 최근것을 등록
        return styleRepository.save(Style.builder()
                .user(user)
                .outer(outer)
                .top(top)
                .bottom(bottom)
                .wearAt(styleUploadRequestDto.getWearAt())
                .highTemp(styleUploadRequestDto.getHighTemp())
                .lowTemp(styleUploadRequestDto.getLowTemp())
                .build())
                .getStyleId();
    }

    public void updateClothWearAt(Cloth cloth) {
        if(cloth == null) return;
        Cloth foundCloth = clothRepository.findByClothId(cloth.getClothId())
                .orElseThrow(() -> new IllegalArgumentException("Cloth not found: " + cloth.getClothId()));
        foundCloth.setWearAt(LocalDate.now());
    }

    private Cloth findClothById(Integer clothId) {
        if (clothId == null) {
            return null;
        }
        return clothRepository.findByClothId(clothId).orElse(null);
    }

    public StyleResponseDto getStyle(int userId, LocalDate date) {
        Style style = styleRepository.findByUser_UserIdAndWearAt(userId, date).orElse(null);

        if (style.getUser().getUserId() != userId) {
            return null;
        }

        if (style == null) return null;

        return StyleResponseDto.builder()
                .styleId(style.getStyleId())
                .outer(style.getOuter())
                .top(style.getTop())
                .bottom(style.getBottom())
                .highTemp(style.getHighTemp())
                .lowTemp(style.getLowTemp())
                .build();
    }

    // 피드백 기능
    @Transactional
    public int updateTemperature(StyleFeedbackRequestDto requestDto) {
        // style은 상의, 하의는 무조건 있음. 아우터는 있을 수도 있고, 없을 수도 있음.
        Style style = styleRepository.findByStyleId(requestDto.getStyleId()).orElseThrow(() -> new EntityNotFoundException("Style not found"));

        if (style.getFeedback() != null)
            return style.getStyleId();

        int highTemp = style.getHighTemp(); //해당 날의 최고기온
        int lowTemp = style.getLowTemp();  //해당 날의 최저기온

        if (style.getOuter() != null)
            updateClothWearAt(style.getOuter());
        updateClothWearAt(style.getTop());
        updateClothWearAt(style.getBottom());

        Feedback feedback = requestDto.getFeedback();

        if (style.getOuter() != null) { //아우터를 입었을 때
            Cloth outer = style.getOuter();
            int outerHighTemp = outer.getHighTemperature();
            int outerLowTemp = outer.getLowTemperature();

            switch (feedback) { //해당 옷의 온도구간은 16~19
                case HOT: //최저기온 17도에서 해당 아우터 입고 더웠으면, 해당 옷은 더 추울 때 입어야되므로 구간을 내린다
                    int modHighTemp1 = lowTemp - 1; // 16도
                    int modLowTemp1 = outerLowTemp - (outerHighTemp - modHighTemp1);
                    outer.setHighTemperature(modHighTemp1);
                    if (outerLowTemp != INF_LOW_TEMPERATURE) //두꺼운 코트, 두꺼운 패딩은 더우면 최대 온도만 낮추고, 최저 온도는 그대로
                        outer.setLowTemperature(modLowTemp1);
                    clothRepository.save(outer);
                    style.updateFeedback(feedback);
                    break;

                case GOOD:
                    style.updateFeedback(feedback);
                    break;

                case COLD: //최저기온 17도에서 해당 아우터 입고 추웠으면, 해당 옷은 더 더울 때 입어야되므로 구간을 올린다
                    int modLowTemp2 = lowTemp + 1;
                    int modHighTemp2 = outerHighTemp + (modLowTemp2 - outerLowTemp);
                    if (outerLowTemp != INF_LOW_TEMPERATURE) { //두꺼운 코트, 두꺼운 패딩은 추워도 입어야한다.
                        outer.setHighTemperature(modHighTemp2);
                        outer.setLowTemperature(modLowTemp2);
                    }
                    clothRepository.save(outer);
                    style.updateFeedback(feedback);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected feedback: " + feedback);
            }
        } else {
            Cloth top = style.getTop();
            int topHighTemp = top.getSoloHighTemperature();
            int topLowTemp = top.getSoloLowTemperature();

            switch (feedback) {
                case HOT:
                    int modSoloHighTemp1 = highTemp - 1;
                    int modSoloLowTemp1 = topLowTemp - (topHighTemp - modSoloHighTemp1);
                    if (topHighTemp != INF_HIGH_TEMPERATURE) { //반팔은 더워도 입어야한다.
                        top.setSoloHighTemperature(modSoloHighTemp1);
                        top.setSoloLowTemperature(modSoloLowTemp1);
                    }
                    clothRepository.save(top);
                    style.updateFeedback(feedback);
                    break;

                case GOOD:
                    style.updateFeedback(feedback);
                    break;

                case COLD:
                    int modSoloLowTemp2 = highTemp + 1;
                    int modSoloHighTemp2 = topHighTemp + (modSoloLowTemp2 - topLowTemp);
                    if (topHighTemp != INF_HIGH_TEMPERATURE) //반팔은 추우면 최저온도만 높인다.
                        top.setSoloHighTemperature(modSoloHighTemp2);
                    top.setSoloLowTemperature(modSoloLowTemp2);
                    clothRepository.save(top);
                    style.updateFeedback(feedback);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected feedback: " + feedback);
            }
        }

        style.updateFeedback(feedback);

        return styleRepository.save(style).getStyleId();
    }
}
