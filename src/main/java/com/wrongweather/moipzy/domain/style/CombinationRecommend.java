package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;

import java.util.List;
import java.util.Map;

import static com.wrongweather.moipzy.domain.clothes.category.SmallCategory.T_SHIRT;

public class CombinationRecommend implements StyleRecommender {

    //옷 조합은 + 로 구분, 두께는 - 로 구분
    //OUTER : 패딩, 코트를 제외한 아우터
    //패딩, 코트는 명시적으로 주어질 예정
    //SWEATER = HOODIE, SWEAT_SHIRT, KNIT
    //T_SHIRT 두께 X
    //COAT THIN,THICK
    private final Map<Integer, String> combinations = Map.ofEntries(
            Map.entry(1, "T_SHIRT"),
            Map.entry(2, "POLO_SHIRT"),
            Map.entry(3, "T_SHIRT+THIN-D_SHIRT"),
            Map.entry(4, "THIN-LONG_SLEEVE"),
            Map.entry(5, "T_SHIRT+NORMAL-D_SHIRT"),
            Map.entry(6, "T_SHIRT+THIN-OUTER"),
            Map.entry(7, "NORMAL-LONG_SLEEVE"), //여기까지 3번
            Map.entry(8, "NORMAL-LONG_SLEEVE+NORMAL-D_SHIRT"),
            Map.entry(9, "NORMAL-LONG_SLEEVE+THIN-OUTER"),
            Map.entry(10, "THIN-SWEATER"), //4번
            Map.entry(11, "NORMAL-LONG_SLEEVE+THICK-D_SHIRT"),
            Map.entry(12, "NORMAL-LONG_SLEEVE+NORMAL-OUTER"),
            Map.entry(13, "NORMAL-SWEATER"), //5번
            Map.entry(14, "NORMAL-D_SHIRT+NORMAL-OUTER"),
            Map.entry(15, "THICK-SWEATER"), //6번
            Map.entry(16, "NORMAL-D_SHIRT+THIN-COAT"),
            Map.entry(17, "NORMAL-SWEATER+NORMAL-OUTER"),
            Map.entry(18, "NORMAL-SWEATER+THIN-COAT"), //7번
            Map.entry(19, "THICK-SWEATER+NORMAL-OUTER"),
            Map.entry(20, "NORMAL-SWEATER+THICK-OUTER"),
            Map.entry(21,"NORMAL-SWEATER+THICK-COAT"),
            Map.entry(22,"THICK-SWEATER+THICK-OUTER"),
            Map.entry(23, "THICK-SWEATER+THICK-PADDING"),
            Map.entry(24,"THICK-SWEATER+THICK-COAT")
    );

    private final int[][] colorCombination = new int[][]{

    };

    @Override
    public List<StyleRecommendResponseDto> recommend() {

        return List.of();
    }
}
