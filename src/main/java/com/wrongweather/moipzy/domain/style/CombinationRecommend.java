package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.temperature.OuterTempRange;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.TopTempRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CombinationRecommend implements StyleRecommender {

    private final ClothRepository clothRepository;

    //옷 조합은 + 로 구분, 두께는 - 로 구분
    //OUTER : 패딩, 코트를 제외한 아우터, 린넨 바지 전부 안되는거로
    //패딩, 코트는 명시적으로 주어질 예정
    //SWEATER = HOODIE, SWEAT_SHIRT, KNIT
    //T_SHIRT 두께 X, 두께 없으면 Degree.NORMAL로 저장
    //COAT THIN,THICK
    //앞쪽이 이너, 뒷쪽이 아우터
    private final Map<Integer, String> clothCombinations = Map.ofEntries(
            Map.entry(1, "NORMAL-T_SHIRT"),
            Map.entry(2, "NORMAL-POLO_SHIRT"), //1번
            Map.entry(3, "NORMAL-T_SHIRT+THIN-D_SHIRT"),
            Map.entry(4, "THIN-LONG_SLEEVE"), //2번
            Map.entry(5, "NORMAL-T_SHIRT+NORMAL-D_SHIRT"),
            Map.entry(6, "NORMAL-T_SHIRT+THIN-OUTER"),
            Map.entry(7, "NORMAL-LONG_SLEEVE"), //3번
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
            Map.entry(21,"NORMAL-SWEATER+THICK-COAT"), //8번
            Map.entry(22,"THICK-SWEATER+THICK-OUTER"),
            Map.entry(23, "THICK-SWEATER+THICK-PADDING"),
            Map.entry(24,"THICK-SWEATER+THICK-COAT") //9번
    );

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

    private final Map<Integer, List<Degree>> bottomRange = new HashMap<>() {{ // 바지 두께 구간
        put(1, Arrays.asList(Degree.THIN));
        put(2, Arrays.asList(Degree.THIN, Degree.LTHIN));
        put(3, Arrays.asList(Degree.LTHIN, Degree.NORMAL));
        put(4, Arrays.asList(Degree.LTHIN, Degree.NORMAL));
        put(5, Arrays.asList(Degree.NORMAL, Degree.LTHICK));
        put(6, Arrays.asList(Degree.NORMAL, Degree.LTHICK));
        put(7, Arrays.asList(Degree.NORMAL, Degree.LTHICK, Degree.THICK));
        put(8, Arrays.asList(Degree.NORMAL, Degree.LTHICK, Degree.THICK));
        put(9, Arrays.asList(Degree.NORMAL, Degree.LTHICK, Degree.THICK));
    }};

    private final List<SmallCategory> outerCategories = Arrays.asList(
            SmallCategory.CARDIGAN, SmallCategory.DENIM_JACKET,
            SmallCategory.BLOUSON, SmallCategory.BLAZER, SmallCategory.LEATHER_JACKET,
            SmallCategory.HOODED, SmallCategory.MA1, SmallCategory.STADIUM_JACKET
    );

    private final List<SmallCategory> sweaterCategories = Arrays.asList(
            SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT
    );

    @Override
    public List<StyleRecommendResponseDto> recommend(TemperatureRange range, int feelTemp) {

        List<StyleRecommendResponseDto> styleResponseDtos = new ArrayList<>();

        //상의 리스트 뽑고, 하의 리스트 뽑아서
        //상의 랜덤으로 한 개 뽑고, 해당 랜덤 상의와 하의 리스트를 안입은 순서로 정렬하고 랜덤으로 한 개씩 뽑으면서
        //상의와 해당 바지가 combination 에 있다면 해당 옷을 등록

        //온도 구간에 따라 바지 두께를 토대로 바지 최대 2개 추출
        //List<Cloth> bottomList = getBottom(feelTemp);

        //온도 구간에 따라 옷 조합 추출
        //ex) <<ClothItem(T) + ClothItem(D_SHIRT)>, <ClothItem(LONG_SLEEVE)>>
        List<List<ClothItem>> combinations = parseCombinations(range, feelTemp);

        //옷 조합을 토대로 상의 리스트 추출
        //상의만 추천된 경우 ex) 얇은 롱슬리브: 얇은 롱슬리브의 리스트 추출 -> List<List<Cloth>>
        //상의, 아우터 추천된 경우 ex) 티셔츠 + 얇은셔츠: 티셔츠 리스트 추출 + 얇은셔츠 리스트 추출 -> List<List<Cloth>>
        //상의+아우터 : List<List<Cloth>> -> List<Cloth> 1번 index가 이너, 2번 index가 아우터
        //상의 :  List<List<Cloth>> _> 1번 index가 이너
        //상의 조합 최대 3개 추천
        //바지에 맞도록 상의 추천
        //일교차가 크면 최고기온이 더우면 안에는 얇게, 최저기온 추우면 겉옷은 두껍게

        // 상하의 : <null, top, bottom>
        // 아우터 : <outer, top, bottom>
        List<List<Cloth>> validCombination = new ArrayList<>();
        for (List<ClothItem> combination : combinations) {
            System.out.println("combination 1's type: " + combination.get(0).getType());
            if (combination.size() == 1) { //상의만 있는 경우

                //smallCategory 및 Degree 추출
                List<SmallCategory> smallCategory = toSmallCategory(combination.get(0).getType());
                Degree degree = toDegree(combination.get(0).getThickness());

                //SmallCategory 와 Degree를 토대로 상의 리스트 추출
                List<Cloth> topList = clothRepository.findAllBySmallCategoryAndDegree(smallCategory, degree);

                //최근에 안입은 순서대로 sorting
                List<Cloth> sortedTopList= topList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                //바지를 Degree 를 토대로 추출
                List<Cloth> bottomList = clothRepository.findAllByDegreeAndLargeCategory(getBottomDegree(feelTemp), LargeCategory.BOTTOM);

                //최근에 안입은 순서대로 sorting
                List<Cloth> sortedBottomList = bottomList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                for (Cloth top : sortedTopList) {
                    for (Cloth bottom : sortedBottomList) {
                        // Check if category combination is valid
                        List<SmallCategory> invalidBottomCategories = topBottomNotGoodCombination.get(top.getSmallCategory());
                        if (invalidBottomCategories != null && invalidBottomCategories.contains(bottom.getSmallCategory())) {
                            continue; // Skip this combination
                        }

                        // Check if color combination is valid
                        List<Color> invalidBottomColors = topBottomNotGoodColorCombination.get(top.getColor());
                        if (invalidBottomColors != null && invalidBottomColors.contains(bottom.getColor()) && top.getColor() != bottom.getColor()) {
                            continue; // Skip this combination
                        }

                        validCombination.add(Arrays.asList(null, top, bottom));
                    }
                }
//                System.out.println("상의 1개만 추천됐을 때 하의 id: " + bottom.getClothId());
//                styleResponseDtos.add(StyleRecommendResponseDto.builder()
//                        .topId(top.getClothId())
//                        .bottomId(bottom.getClothId())
//                        .build());
            }
            else if(combination.size() == 2) {

                // 상의 리스트 추출 및 sorting
                List<SmallCategory> innerCategoryList = toSmallCategory(combination.get(0).getType());
                Degree innerDegree = toDegree(combination.get(0).getThickness());
                //System.out.println("2개 추천됐을 때 상의 CategoryList: " + innerCategoryList);
                List<Cloth> topList = clothRepository.findAllBySmallCategoryAndDegree(innerCategoryList, innerDegree);
                List<Cloth> sortedTopList= topList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                //아우터 리스트 추출 및 sorting
                List<SmallCategory> outerCategoryList = toSmallCategory(combination.get(1).getType());
                Degree outerDegree = toDegree(combination.get(1).getThickness());
                //System.out.println("2개 추천됐을 때 상의 CategoryList: " + outerCategoryList);
                List<Cloth> outerList = clothRepository.findAllBySmallCategoryAndDegree(outerCategoryList, outerDegree);
                List<Cloth> sortedOuterList= outerList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                //바지를 Degree 를 토대로 추출
                List<Cloth> bottomList = clothRepository.findAllByDegreeAndLargeCategory(getBottomDegree(feelTemp), LargeCategory.BOTTOM);

                //최근에 안입은 순서대로 sorting
                List<Cloth> sortedBottomList = bottomList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                List<List<Cloth>> styles = new ArrayList<>();

                //아우터와 하의 먼저 for 문을 통해 valid combination 추출
                for (Cloth outer : sortedOuterList) {
                    for (Cloth bottom : sortedBottomList) {
                        // Check if category combination is valid
                        List<SmallCategory> invalidBottomCategories = outerBottomNotGoodCombination.get(outer.getSmallCategory());
                        if (invalidBottomCategories != null && invalidBottomCategories.contains(bottom.getSmallCategory())) {
                            continue; // Skip this combination
                        }

                        // Check if color combination is valid
                        List<Color> invalidBottomColors = outerBottomNotGoodColorCombination.get(outer.getColor());
                        if (invalidBottomColors != null && invalidBottomColors.contains(bottom.getColor())) {
                            continue; // Skip this combination
                        }

                        styles.add(Arrays.asList(outer, null , bottom));
                    }
                }

                //아우터 상의 for 문을 통해 valid combination 추출
                for (List<Cloth> style : styles) {
                    // Check if category combination is valid
                    Cloth outer = style.get(0);
                    for (Cloth top : sortedTopList) {
                        List<SmallCategory> invalidTopCategories = outerTopNotGoodCombination.get(outer.getSmallCategory());
                        if (invalidTopCategories != null && invalidTopCategories.contains(top.getSmallCategory())) {
                            continue; // Skip this combination
                        }

                        // Check if color combination is valid
                        List<Color> invalidTopColors = outerTopNotGoodColorCombination.get(outer.getColor());
                        if (invalidTopColors != null && invalidTopColors.contains(top.getColor()) && outer.getColor() != top.getColor()) {
                            continue; // Skip this combination
                        }

                        style.set(1, top);
                    }

                }
            }
        }

        return styleResponseDtos;
    }

    @Override
    public List<List<Cloth>> recommendByHighLow(OuterTempRange outerTempRange, TopTempRange topTempRange, int highTemp, int lowTemp) {

        String outerStr = "7,10"; //"THICK-MEDIUM_OUTER", "THICK-COAT
        String topStr = "4,7"; //"NORMAL-LONG_SLEEVE", "NORMAL-SWEATER"

        System.out.println("recommendByHighLow");

        // 문자열을 숫자로 변경
        List<Integer> outerInt = convertStringToList(outerStr); //보통 MEDIUM 아우터, 두꺼운 코트
        List<Integer> topInt = convertStringToList(topStr);  //롱슬리브, (보통스웨터)

        // 숫자를 토대로 옷 종류 얻음
        List<String> outerCategory = outerInt.stream().map(outerClothing::get).filter(Objects::nonNull).collect(Collectors.toList()); //NORMAL-MEDIUM_OUTER, THICK-COAT
        List<String> topCategory = topInt.stream().map(topClothing::get).filter(Objects::nonNull).collect(Collectors.toList()); // NORMAL-LONG_SLEEVE, NORMAL-SWEATER

        // 두께와 옷 종류 구분
        List<ClothItem> outerItems = parseThicknessAndCategory(outerCategory); //(NORMAL, MEDIUM_OUTER), (THICK, COAT)
        List<ClothItem> topItems = parseThicknessAndCategory(topCategory); //(NORMAL, LONG_SLEEVE), (NORMAl, SWEATER)

        // 평균온도 계산
        int feelTemp = (highTemp+lowTemp)/2;

        // 평균온도를 토대로 바지 리스트 얻음
        List<Cloth> bottomList = clothRepository.findAllByDegreeAndLargeCategory(getBottomDegree(feelTemp), LargeCategory.BOTTOM);

        //Outer, Top, Bottom 추출 및 최근에 안입은 순서대로 sorting
        List<Cloth> sortedOuterList = getClothAndSort(outerItems);
        List<Cloth> sortedTopList = getClothAndSort(topItems);
        List<Cloth> sortedBottomList = bottomList.stream().sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder()))).toList();

        for (Cloth outer : sortedOuterList) {
            System.out.print(outer.getClothId()+" ");
        }
        System.out.println();
        for (Cloth top : sortedTopList) {
            System.out.print(top.getClothId()+" ");
        }
        System.out.println();
        for (Cloth bottom : sortedBottomList) {
            System.out.print(bottom.getClothId()+" ");
        }
        System.out.println();

        List<List<Cloth>> styles = new ArrayList<>();

        //겉옷, 하의 먼저 valid combination 추출
        for (Cloth outer : sortedOuterList) {
            for (Cloth bottom : sortedBottomList) {
                List<SmallCategory> invalidBottomCategories = outerBottomNotGoodCombination.get(outer.getSmallCategory());
                if (invalidBottomCategories != null && invalidBottomCategories.contains(bottom.getSmallCategory())) {
                    continue; // Skip this combination
                }

                // Check if color combination is valid
                List<Color> invalidBottomColors = outerBottomNotGoodColorCombination.get(outer.getColor());
                if (invalidBottomColors != null && invalidBottomColors.contains(bottom.getColor()) && outer.getColor() != bottom.getColor()) {
                    continue; // Skip this combination
                }

                styles.add(Arrays.asList(outer, null, bottom));
            }
        }

        for (List<Cloth> style : styles) {
            for (Cloth top : sortedTopList) {
                style.set(1, top);
            }
        }

        return styles;
    }

    /**
     *
     * @param range
     * @param feelTemp
     * @return feelTemp를 토대로 해당 구간에 있는 옷의 조합의 번호를 추출하여 List<Integer>로 반환
     */

    //----------------------------------------------
    //recommend method 에서 사용하는 method
    private List<Integer> getRange(TemperatureRange range, int feelTemp) {
        String temp;
        if (feelTemp >= 28) {
            temp = range.getOver28();
        } else if (feelTemp >= 24) {
            temp = range.getBetween27_24();
        } else if (feelTemp >= 20) {
            temp = range.getBetween23_20();
        } else if (feelTemp >= 17) {
            temp = range.getBetween19_17();
        } else if (feelTemp >= 14) {
            temp = range.getBetween16_14();
        } else if (feelTemp >= 11) {
            temp = range.getBetween13_11();
        } else if (feelTemp >= 8) {
            temp = range.getBetween10_8();
        } else if (feelTemp >= 5) {
            temp = range.getBetween7_5();
        } else {
            temp = range.getUnder4();
        }

        return Arrays.stream(temp.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<ClothItem> parseClothingCombination(int combId) {
        //"T_SHIRT+THIN-D_SHIRT"
        String combValue = clothCombinations.get(combId);
        if (combValue == null) return Collections.emptyList();

        //문자열을 +로 우선 나눈다음, 두께를 파악하기 위해 parseItem 메서드를 사용
        //결과적으로 List<T_SHIRT, D_SHIRT>
        return Arrays.stream(combValue.split("\\+"))
                .map(this::parseItem)
                .collect(Collectors.toList());

    }

    private ClothItem parseItem(String item) {
        //문자열을 -로 우선 나눔
        String[] parts = item.split("-");
        //앞쪽이 두께이므로 parts[0]을 두께에 넣기
        String thickness;
        String type;

        if (parts.length == 1) { //두께 없이 옷의 종류만 있을 경우 ex)T_SHIRT, POLO_SHIRT
            thickness = "NULL";
            type = parts[0];
        } else { //두께, 옷 종류 모두 존재
            thickness = parts[0];
            type = parts[1];
        }

        return new ClothItem(thickness, type);
    }

    private Degree toDegree(String thickness) {
        switch (thickness) {
            case "THIN" -> {
                return Degree.THIN;
            }
            case "THICK" -> {
                return Degree.THICK;
            }
            case "NORMAL" -> {
                return Degree.NORMAL;
            }
        }
        return Degree.NORMAL;
    }

    private List<SmallCategory> toSmallCategory(String type) {
        //T_SHIRT, POLO_SHIRT, LONG_SLEEVE, D_SHIRT, OUTER, SWEATER, COAT, PADDING
        switch (type) {
            case "T_SHIRT" -> {
                return Arrays.asList(SmallCategory.T_SHIRT);
            }
            case "POLO_SHIRT" -> {
                return Arrays.asList(SmallCategory.POLO_SHIRT);
            }
            case "LONG_SLEEVE" -> {
                return Arrays.asList(SmallCategory.LONG_SLEEVE);
            }
            case "D_SHIRT" -> {
                return Arrays.asList(SmallCategory.D_SHIRT);
            }
            case "OUTER" -> {
                return outerCategories;
            }
            case "PADDING" -> {
                return Arrays.asList(SmallCategory.PADDING);
            }
            case "COAT" -> {
                return Arrays.asList(SmallCategory.COAT);
            }
            case "SWEATER" -> {
                return sweaterCategories;
            }
            case "LIGHT-OUTER" -> {
                return lightOuterCategories;
            }
            case "MEDIUM-OUTER" -> {
                return mediumOuterCategories;
            }
        }
        return null;
    }

    private List<Degree> getBottomDegree(int feelTemp) {
        List<Degree> bottomDegree = null;

        //온도구간으로 바지 두께 추출
        if (feelTemp >= 28) {
            bottomDegree =  bottomRange.get(1);
        } else if (feelTemp >= 24) {
            bottomDegree =  bottomRange.get(2);
        } else if (feelTemp >= 20) {
            bottomDegree =  bottomRange.get(3);
        } else if (feelTemp >= 17) {
            bottomDegree =  bottomRange.get(4);
        } else if (feelTemp >= 14) {
            bottomDegree =  bottomRange.get(5);
        } else if (feelTemp >= 11) {
            bottomDegree =  bottomRange.get(6);
        } else if (feelTemp >= 8) {
            bottomDegree =  bottomRange.get(7);
        } else if (feelTemp >= 5) {
            bottomDegree =  bottomRange.get(8);
        } else {
            bottomDegree =  bottomRange.get(9);
        }

        return bottomDegree;
    }

    private List<List<ClothItem>> parseCombinations(TemperatureRange range, int feelTemp) {
        // 각 구간의 옷 조합은 String("1,2") 과 같이 표현돼있음
        // 온도 구간에서 feelTemp가 속해있는 구간을 추출하여 List<Integer>로 반환
        List<Integer> combIds = getRange(range, feelTemp);

        // 각 옷 조합 번호를 ClothItem으로 파싱
        // <3,4>는 <<ClothItem(T) + ClothItem(D_SHIRT)>, <ClothItem(LONG_SLEEVE)>>
        List<List<ClothItem>> combinations = new ArrayList<>();
        for (int combId : combIds) {
            combinations.add(parseClothingCombination(combId));
        }
        return combinations;
    }
    //----------------------------------------------


    //----------------------------------------------
    private List<Integer> convertStringToList(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<ClothItem> parseThicknessAndCategory(List<String> inputs) {
        List<ClothItem> thicknessAndCategory = new ArrayList<>();
        for (String input : inputs) {
            String[] parts = input.split("-");
            String thickness = parts[0];
            String category = parts[1];
            thicknessAndCategory.add(new ClothItem(thickness, category));
        }

        return thicknessAndCategory;
    }

    private List<Cloth> getClothAndSort(List<ClothItem> clothItems) {
        List<Cloth> clothes = new ArrayList<>();
        for (ClothItem clothItem : clothItems) {
            Degree degree = toDegree(clothItem.getThickness());
            List<SmallCategory> smallCategory = toSmallCategory(clothItem.getType());

            List<Cloth> outerList = clothRepository.findAllBySmallCategoryAndDegree(smallCategory, degree);
            clothes.addAll(outerList);
        }

        return clothes.stream().sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder()))).toList();
    }
    //----------------------------------------------
}

