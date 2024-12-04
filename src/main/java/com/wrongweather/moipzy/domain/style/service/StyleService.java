package com.wrongweather.moipzy.domain.style.service;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StyleService {

    private final ClothRepository clothRepository;
    private final StyleRepository styleRepository;
    private final UserRepository userRepository;
    private final ChatGPTService chatGPTService;
    private final CalendarService calendarService;

    private final int INF_HIGH_TEMPERATURE = 70;
    private final int INF_LOW_TEMPERATURE = -70;

    public List<StyleRecommendResponseDto> recommendTest(int userId, int highTemp, int lowTemp) {
        String prompt = "High temperature: "+ highTemp
                     + ", Low temperature: "+ lowTemp + "\n";

        String eventSummary = null;

        //해당 유저의 구글 캘린더에서 일정을 가져오고, 일정이 있으면 prompt에 추가한다.
        Map<LocalDate, List<Map<String, String>>> eventList = new HashMap<>();
        try {
             //eventList = calendarService.getEvents(userId, LocalDate.now());

             prompt += "Event: ";

             // 각 날짜별 이벤트에서 summary만 추출
             for (Map.Entry<LocalDate, List<Map<String, String>>> entry : eventList.entrySet()) {
                 LocalDate date = entry.getKey();
                 List<Map<String, String>> events = entry.getValue();

                 // 각 이벤트에서 summary 추출
                 StringBuilder eventDetails = new StringBuilder();
                 for (Map<String, String> event : events) {
                     String summary = event.get("summary");
                     String startTime = event.get("startTime");
                     String endTime = event.get("endTime");

                     if (summary != null && startTime != null && endTime != null) {
                         // startTime과 endTime에서 시각 추출
                         DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                         LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
                         LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

                         String startFormatted = startDateTime.getHour() + "시 " + startDateTime.getMinute() + "분";
                         String endFormatted = endDateTime.getHour() + "시 " + endDateTime.getMinute() + "분";

                         // 이벤트 상세 정보 추가
                         eventDetails.append("Summary: ").append(summary)
                                 .append(", Start: ").append(startFormatted)
                                 .append(", End: ").append(endFormatted)
                                 .append("; ");
                     }
                 }

                 //날짜와 summary 출력
                 if (eventDetails.length() > 0) {
                     // 마지막 쉼표 제거
                     eventDetails.setLength(eventDetails.length() - 2);
                     eventSummary += "Date: " + date + " - " + eventDetails.toString() + "\n";
                 }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 최고기온, 최저기온에 따라 아우터, 상의, 하의 리스트 추출
        List<Cloth> outer = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.OUTER, lowTemp, userId); //조회 결과가 없으면 빈 리스트를 반환한다. null 아님.
        List<Cloth> top = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.TOP, highTemp, userId);
        List<Cloth> bottom = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.BOTTOM, (highTemp+lowTemp)/2, userId);
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

    public List<StyleRecommendResponseDto> recommend(int userId, int highTemp, int lowTemp, String events) {
        String prompt = "High temperature: "+ highTemp
                + ", Low temperature: "+ lowTemp + "\n";

        prompt += events;

        // 최고기온, 최저기온에 따라 아우터, 상의, 하의 리스트 추출
        List<Cloth> outer = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.OUTER, lowTemp, userId); //조회 결과가 없으면 빈 리스트를 반환한다. null 아님.
        List<Cloth> top = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.TOP, highTemp, userId);
        List<Cloth> bottom = clothRepository.findByLargeCategoryAndTemperatureInRangeAndUserId(LargeCategory.BOTTOM, (highTemp+lowTemp)/2, userId);
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

        User user = userRepository.findByUserId(styleUploadRequestDto.getUserId()).orElse(null);

        // 각 부위 Cloth entity or null 추출
        Cloth outer = findClothById(styleUploadRequestDto.getOuterId());
        Cloth top = findClothById(styleUploadRequestDto.getTopId());
        Cloth bottom = findClothById(styleUploadRequestDto.getBottomId());

        // 각 옷의 wearAt 오늘 날짜로 갱신
        updateClothWearAt(outer);
        updateClothWearAt(top);
        updateClothWearAt(bottom);

        return styleRepository.save(Style.builder()
                .user(user)
                .outer(outer)
                .top(top)
                .bottom(bottom)
                .highTemp(styleUploadRequestDto.getHighTemp())
                .lowTemp(styleUploadRequestDto.getLowTemp())
                .build())
                .getStyleId();
    }

    @Transactional
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

    public int updateTemperature(StyleFeedbackRequestDto requestDto) {
        // style은 상의, 하의는 무조건 있음. 아우터는 있을 수도 있고, 없을 수도 있음.
        Style style = styleRepository.findByStyleId(requestDto.getStyleId()).orElseThrow(() -> new EntityNotFoundException("Style not found"));
        int highTemp = style.getHighTemp(); //해당 날의 최고기온
        int lowTemp = style.getLowTemp();  //해당 날의 최저기온
        User user = userRepository.findByUserId(style.getUser().getUserId()).orElse(null);
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
                    break;

                case GOOD:
                    break;

                case COLD: //최저기온 17도에서 해당 아우터 입고 추웠으면, 해당 옷은 더 더울 때 입어야되므로 구간을 올린다
                    int modLowTemp2 = lowTemp + 1;
                    int modHighTemp2 = outerHighTemp + (modLowTemp2 - outerLowTemp);
                    if (outerLowTemp != INF_LOW_TEMPERATURE) { //두꺼운 코트, 두꺼운 패딩은 추워도 입어야한다.
                        outer.setHighTemperature(modHighTemp2);
                        outer.setLowTemperature(modLowTemp2);
                    }
                    clothRepository.save(outer);
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
                    break;

                case GOOD:
                    break;

                case COLD:
                    int modSoloLowTemp2 = highTemp + 1;
                    int modSoloHighTemp2 = topHighTemp + (modSoloLowTemp2 - topLowTemp);
                    if (topHighTemp != INF_HIGH_TEMPERATURE) //반팔은 추우면 최저온도만 높인다.
                        top.setSoloHighTemperature(modSoloHighTemp2);
                    top.setSoloLowTemperature(modSoloLowTemp2);
                    clothRepository.save(top);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected feedback: " + feedback);
            }
        }

        style.updateFeedback(feedback);

        return styleRepository.save(style).getStyleId();
    }

}
