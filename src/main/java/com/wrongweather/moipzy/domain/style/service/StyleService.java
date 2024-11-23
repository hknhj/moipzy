package com.wrongweather.moipzy.domain.style.service;

import com.wrongweather.moipzy.domain.calendar.service.CalendarService;
import com.wrongweather.moipzy.domain.chatGPT.service.ChatGPTService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.style.CombinationRecommend;
import com.wrongweather.moipzy.domain.style.StyleRepository;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.dto.StyleUploadRequestDto;
import com.wrongweather.moipzy.domain.temperature.OuterTempRange;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.TopTempRange;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StyleService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ClothRepository clothRepository;
    private final StyleRepository styleRepository;
    private final UserRepository userRepository;
    private final CombinationRecommend combinationRecommend;
    private final ChatGPTService chatGPTService;
    private final CalendarService calendarService;

    public List<StyleRecommendResponseDto> recommend(int userId, int feelTemp) {

        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException());
        TemperatureRange range = user.getRange();
        System.out.println(range.getRangeId());
        System.out.println(range.getBetween27_24());

        return combinationRecommend.recommend(range, feelTemp);
    }

    public List<List<Cloth>> recommendByHighLow(int userId, int highTemp, int lowTemp) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException());
        System.out.println("userId: "+user.getUserId());
        OuterTempRange outerTempRange = new OuterTempRange();
        TopTempRange topTempRange = new TopTempRange();
        List<List<Cloth>> recommended = combinationRecommend.recommendByHighLow(outerTempRange, topTempRange, highTemp, lowTemp);
        for (List<Cloth> style : recommended ){
            System.out.print("list of cloth of style: ");
            for (Cloth cloth : style) {
                System.out.print(cloth.getClothId()+" ");
            }
            System.out.println();
        }
        return recommended;
    }

    public String recommendTest(int highTemp, int lowTemp) {
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
        List<Integer> ids = Arrays.asList(styleUploadRequestDto.getOuterId(),
                styleUploadRequestDto.getTopId(), styleUploadRequestDto.getBottomId());
        List<Cloth> clothes = clothRepository.findAllByOptionalIds(ids.get(0), ids.get(1), ids.get(2));

        User user = clothes.get(clothes.size()-1).getUser(); //하의는 무조건 있으므로 상의에서 user의 정보를 받아온다.

        //불러운 옷이 없을 때 예외 추가 필요

        List<Cloth> order = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            order.add(null);
        }

        //outer, semiOuter, top, bottom 순서대로 array에 등록
        List<Integer> clothIds = new ArrayList<>();
        for (Cloth cloth : clothes) {
            if (cloth.getClothId() == ids.get(0))
                order.set(0, cloth);
            else if (cloth.getClothId() == ids.get(1))
                order.set(1, cloth);
            else if(cloth.getClothId() == ids.get(2))
                order.set(2, cloth);
            else
                order.set(3, cloth);
            clothIds.add(cloth.getClothId());
        }

        //각 옷의 wearAt 업데이트
        updateClothesWearAt(clothIds);

        return styleRepository.save(styleUploadRequestDto.toEntity(user, order.get(0), order.get(1), order.get(2))).getStyleId();
    }

    @Transactional
    public void updateClothesWearAt(List<Integer> clothIds) {
        entityManager.createQuery("UPDATE Cloth c SET c.wearAt = :now WHERE c.clothId IN :ids")
                .setParameter("now", LocalDate.now())
                .setParameter("ids", clothIds)
                .executeUpdate();
    }
}
