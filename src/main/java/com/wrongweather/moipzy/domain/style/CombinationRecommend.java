package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CombinationRecommend implements StyleRecommender {

    private final ClothRepository clothRepository;

    private final Map<Integer, String> topClothing = new HashMap<>() {{
        put(1, "NORMAL-T_SHIRT");
        put(2, "NORMAL-POLO_SHIRT");
        put(3, "THIN-LONG_SLEEVE");
        put(4, "NORMAL-LONG_SLEEVE");
        put(5, "NORMAL-D_SHIRT");
        put(6, "THIN-SWEATER");
        put(7, "NORMAL-SWEATER");
        put(8, "THICK-SWEATER");
    }};
    //sweaterCategories 는 밑에 있음 그거 써라

    private final Map<Integer, String> outerClothing = new HashMap<>() {{
        put(1, "THIN-D_SHIRT");
        put(2, "NORMAL-D_SHIRT");
        put(3, "THICK-D_SHIRT");
        put(4, "THIN-LIGHT_OUTER");
        put(5, "NORMAL-LIGHT_OUTER");
        put(6, "THICK-LIGHT_OUTER");
        put(7, "NORMAL-MEDIUM_OUTER");
        put(8, "THICK-MEDIUM_OUTER");
        put(9, "THIN-PADDING");
        put(10, "THIN-COAT");
        put(11, "NORMAL-PADDING");
        put(12, "THICK-COAT");
        put(13, "THICK-PADDING");
    }};
    private final List<SmallCategory> lightOuterCategories = Arrays.asList(
            SmallCategory.CARDIGAN, SmallCategory.DENIM_JACKET, SmallCategory.BLOUSON,
            SmallCategory.BLAZER, SmallCategory.LEATHER_JACKET, SmallCategory.HOODED
    );

    private final List<SmallCategory> mediumOuterCategories = Arrays.asList(
            SmallCategory.MA1, SmallCategory.STADIUM_JACKET, SmallCategory.JACKET
    );


    private final Map<Color, List<Color>> topBottomNotGoodColorCombination = new HashMap<>() {{ // 색 조합
        put(Color.BLUE, Arrays.asList(Color.LIGHTBLUE, Color.DEEPBLUE, Color.KHAKI, Color.BEIGE));
        put(Color.NAVY, null);
        put(Color.GREEN, Arrays.asList(Color.KHAKI, Color.BEIGE));
        put(Color.BLACK, null);
        put(Color.WHITE, Arrays.asList(Color.CREAM));
        put(Color.BEIGE, null);
        put(Color.RED, Arrays.asList(Color.KHAKI));
        put(Color.BROWN, Arrays.asList(Color.KHAKI));
        put(Color.LIGHTGREY, Arrays.asList(Color.CREAM));
    }};

    private final Map<SmallCategory, List<SmallCategory>> topBottomNotGoodCombination = new HashMap<>() {{
        put(SmallCategory.T_SHIRT, null);
        put(SmallCategory.POLO_SHIRT, Arrays.asList(SmallCategory.SWEAT_PANTS, SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.D_SHIRT, null);
        put(SmallCategory.HOODIE, Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.SWEAT_SHIRT, Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.KNIT, Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.LONG_SLEEVE, null);
    }};

    List<SmallCategory> bottomNotGoodWithOuter = Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS);
    private final Map<SmallCategory, List<SmallCategory>> outerBottomNotGoodCombination = new HashMap<>() {{
        put(SmallCategory.D_SHIRT, null);
        put(SmallCategory.CARDIGAN, bottomNotGoodWithOuter);
        put(SmallCategory.DENIM_JACKET, Arrays.asList(SmallCategory.JEANS, SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.BLOUSON, Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS, SmallCategory.SWEAT_PANTS));
        put(SmallCategory.BLAZER, bottomNotGoodWithOuter);
        put(SmallCategory.LEATHER_JACKET, bottomNotGoodWithOuter);
        put(SmallCategory.HOODED, bottomNotGoodWithOuter);
        put(SmallCategory.MA1, bottomNotGoodWithOuter);
        put(SmallCategory.STADIUM_JACKET, bottomNotGoodWithOuter);
        put(SmallCategory.COAT, bottomNotGoodWithOuter);
        put(SmallCategory.PADDING, bottomNotGoodWithOuter);
    }};

    private final Map<Color, List<Color>> outerBottomNotGoodColorCombination = new HashMap<>() {{
        put(Color.NAVY, null);
        put(Color.BLACK, null);
        put(Color.BROWN, Arrays.asList(Color.KHAKI, Color.BEIGE));
        put(Color.BEIGE, null);
        put(Color.LIGHTGREY, Arrays.asList(Color.BEIGE));
        put(Color.RED, Arrays.asList(Color.KHAKI, Color.BEIGE));
        put(Color.BLUE, Arrays.asList(Color.BEIGE));
        put(Color.CHARCOAL, null);
        put(Color.WHITE, Arrays.asList(Color.CREAM, Color.BEIGE));
    }};

    List<SmallCategory> topNotGoodWithOuter = Arrays.asList(SmallCategory.POLO_SHIRT, SmallCategory.D_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT);
    private final Map<SmallCategory, List<SmallCategory>> outerTopNotGoodCombination = new HashMap<>() {{
        put(SmallCategory.D_SHIRT, topNotGoodWithOuter);
        put(SmallCategory.CARDIGAN, Arrays.asList(SmallCategory.POLO_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT));
        put(SmallCategory.DENIM_JACKET, Arrays.asList(SmallCategory.POLO_SHIRT, SmallCategory.D_SHIRT));
        put(SmallCategory.BLOUSON, Arrays.asList(SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT));
        put(SmallCategory.BLAZER, null);
        put(SmallCategory.LEATHER_JACKET, null);
        put(SmallCategory.HOODED, topNotGoodWithOuter);
        put(SmallCategory.MA1, Arrays.asList(SmallCategory.POLO_SHIRT));
        put(SmallCategory.STADIUM_JACKET, Arrays.asList(SmallCategory.POLO_SHIRT));
        put(SmallCategory.COAT, Arrays.asList(SmallCategory.POLO_SHIRT));
        put(SmallCategory.PADDING, null);
    }};

    //같은 색 조건문으로 처리
    private final Map<Color, List<Color>> outerTopNotGoodColorCombination = new HashMap<>() {{
        put(Color.NAVY, Arrays.asList(Color.GREEN));
        put(Color.BLACK, null);
        put(Color.BROWN, Arrays.asList(Color.BLUE, Color.GREEN));
        put(Color.BEIGE, null);
        put(Color.LIGHTGREY, Arrays.asList(Color.GREEN));
        put(Color.RED, Arrays.asList(Color.BLUE, Color.GREEN, Color.BROWN));
        put(Color.BLUE, Arrays.asList(Color.BEIGE));
    }};


    private final List<SmallCategory> outerCategories = Arrays.asList(
            SmallCategory.CARDIGAN, SmallCategory.DENIM_JACKET,
            SmallCategory.BLOUSON, SmallCategory.BLAZER, SmallCategory.LEATHER_JACKET,
            SmallCategory.HOODED, SmallCategory.MA1, SmallCategory.STADIUM_JACKET
    );

    private final List<SmallCategory> sweaterCategories = Arrays.asList(
            SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT
    );

    public List<List<Cloth>> recommendByHighLowTemp(int highTemp, int lowTemp) {
        List<Cloth> outerList = clothRepository.findByLargeCategoryAndTemperatureInRange(LargeCategory.OUTER, lowTemp);
        List<Cloth> topList = clothRepository.findByLargeCategoryAndTemperatureInRange(LargeCategory.TOP, highTemp);
        List<Cloth> bottomList = clothRepository.findByLargeCategoryAndTemperatureInRange(LargeCategory.BOTTOM, (highTemp+lowTemp)/2);
        for (Cloth outer : outerList) {
            System.out.print(outer.getClothId()+" ");
        }
        System.out.println();
        for (Cloth top : topList) {
            System.out.print(top.getClothId()+" ");
        }
        System.out.println();
        for (Cloth bottom : bottomList) {
            System.out.print(bottom.getClothId()+" ");
        }
        System.out.println();
        return Arrays.asList(outerList, topList, bottomList);
    }
}

