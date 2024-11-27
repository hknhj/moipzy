package com.wrongweather.moipzy.domain.style.service;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import com.wrongweather.moipzy.domain.chatGPT.service.ChatGPTService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.style.CombinationRecommend;
import com.wrongweather.moipzy.domain.style.Style;
import com.wrongweather.moipzy.domain.style.StyleRepository;
import com.wrongweather.moipzy.domain.style.dto.StyleResponseDto;
import com.wrongweather.moipzy.domain.style.dto.StyleUploadRequestDto;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StyleService {

    private final ClothRepository clothRepository;
    private final StyleRepository styleRepository;
    private final UserRepository userRepository;
    private final CombinationRecommend combinationRecommend;
    private final ChatGPTService chatGPTService;

    public String recommend(int highTemp, int lowTemp) {
        String prompt = "";
        List<List<Cloth>> clothList = combinationRecommend.recommendByHighLowTemp(highTemp, lowTemp);
        for (List<Cloth> cloth : clothList) {
            for (Cloth cloth1 : cloth) {
                prompt += cloth1.toString() + ' ';
            }
        }
        System.out.println(prompt);
        return chatGPTService.getChatGPTResponse(prompt);
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
}
