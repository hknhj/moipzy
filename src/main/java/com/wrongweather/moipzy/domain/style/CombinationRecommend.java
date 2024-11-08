package com.wrongweather.moipzy.domain.style;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
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
            Map.entry(1, "NORMAL-T_SHIRT"), //바지 모두 가능
            Map.entry(2, "NORMAL-POLO_SHIRT"), //1번 //스웻, 린넨, 반바지 제외
            Map.entry(3, "NORMAL-T_SHIRT+THIN-D_SHIRT"), //다 가능
            Map.entry(4, "THIN-LONG_SLEEVE"), //2번 //다 가능
            Map.entry(5, "NORMAL-T_SHIRT+NORMAL-D_SHIRT"), //다 가능
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

    private final Map<Color, List<Color>> colorCombination = new HashMap<>() {{ // 색 조합
        put(Color.LIGHTBLUE, Arrays.asList(Color.NAVY, Color.GREEN, Color.BEIGE, Color.RED, Color.BLACK, Color.WHITE, Color.BROWN));
        put(Color.DEEPBLUE, Arrays.asList(Color.NAVY, Color.GREEN, Color.BEIGE, Color.BLACK, Color.WHITE, Color.BROWN));
        put(Color.BLACK, Arrays.asList(Color.BLUE, Color.NAVY, Color.GREEN, Color.BEIGE, Color.RED, Color.WHITE, Color.BROWN));
        put(Color.KHAKI, Arrays.asList(Color.NAVY, Color.BEIGE, Color.BLACK, Color.WHITE));
        put(Color.CREAM, Arrays.asList(Color.NAVY, Color.BEIGE, Color.RED, Color.BLACK, Color.BROWN));
        put(Color.BEIGE, Arrays.asList(Color.NAVY, Color.GREEN, Color.RED, Color.BLACK, Color.WHITE));
    }};

    private final Map<SmallCategory, List<SmallCategory>> topBottomCombination = new HashMap<>() {{  // 상하의 조합
        put(SmallCategory.JEANS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.POLO_SHIRT, SmallCategory.D_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT, SmallCategory.LONG_SLEEVE));
        put(SmallCategory.SWEAT_PANTS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.D_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT, SmallCategory.LONG_SLEEVE));
        put(SmallCategory.COTTON_PANTS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.POLO_SHIRT, SmallCategory.D_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT, SmallCategory.LONG_SLEEVE));
        put(SmallCategory.SLACKS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.POLO_SHIRT, SmallCategory.D_SHIRT, SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT, SmallCategory.LONG_SLEEVE));
        put(SmallCategory.LINEN_PANTS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.D_SHIRT, SmallCategory.LONG_SLEEVE));
        put(SmallCategory.SHORTS, Arrays.asList(SmallCategory.T_SHIRT, SmallCategory.D_SHIRT,  SmallCategory.LONG_SLEEVE));
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

    List<SmallCategory> notGoodWithOuter = Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS);
    private final Map<SmallCategory, List<SmallCategory>> outerBottomNotGoodCombination = new HashMap<>() {{
        put(SmallCategory.D_SHIRT, null);
        put(SmallCategory.CARDIGAN, notGoodWithOuter);
        put(SmallCategory.DENIM_JACKET, Arrays.asList(SmallCategory.JEANS, SmallCategory.LINEN_PANTS, SmallCategory.SHORTS));
        put(SmallCategory.BLOUSON, Arrays.asList(SmallCategory.LINEN_PANTS, SmallCategory.SHORTS, SmallCategory.SWEAT_PANTS));
        put(SmallCategory.BLAZER, notGoodWithOuter);
        put(SmallCategory.LEATHER_JACKET, notGoodWithOuter);
        put(SmallCategory.HOODED, notGoodWithOuter);
        put(SmallCategory.MA1, notGoodWithOuter);
        put(SmallCategory.STADIUM_JACKET, notGoodWithOuter);
        put(SmallCategory.COAT, notGoodWithOuter);
        put(SmallCategory.PADDING, notGoodWithOuter);
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

    private final List<SmallCategory> outerCategories = Arrays.asList(SmallCategory.CARDIGAN, SmallCategory.DENIM_JACKET,
            SmallCategory.BLOUSON, SmallCategory.BLAZER, SmallCategory.LEATHER_JACKET,
            SmallCategory.HOODED, SmallCategory.MA1, SmallCategory.STADIUM_JACKET);

    private final List<SmallCategory> sweaterCategories = Arrays.asList(SmallCategory.HOODIE, SmallCategory.SWEAT_SHIRT, SmallCategory.KNIT);



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
        for (List<ClothItem> combination : combinations) {
            System.out.println("combination 1's type: " + combination.get(0).getType());
            if (combination.size() == 1) { //상의만 있는 경우
                List<SmallCategory> smallCategory = toSmallCategory(combination.get(0).getType());
                Degree degree = toDegree(combination.get(0).getThickness());
                List<Cloth> topList = clothRepository.findAllBySmallCategoryAndDegree(smallCategory, degree);
                List<Cloth> sortedTopList= topList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();
                Cloth top = sortedTopList.get(0);
                System.out.println("상의 1개만 추천됐을 때 상의 id: " + top.getClothId());

                List<Cloth> bottomList = clothRepository.findAllByDegreeAndLargeCategory(getBottomDegree(feelTemp), LargeCategory.BOTTOM);
                List<Cloth> sortedBottomList = bottomList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();
                SmallCategory topCategory = top.getSmallCategory();
                Cloth bottom = null;
                for (Cloth sortedBottom : sortedBottomList) {
                    SmallCategory bottomCategory = sortedBottom.getSmallCategory();

                    List<SmallCategory> notGoodBottoms = topBottomNotGoodCombination.get(topCategory);

                    if(notGoodBottoms == null || !notGoodBottoms.contains(bottomCategory)) {
                        bottom = sortedBottom;
                        break;
                    }
                }
                System.out.println("상의 1개만 추천됐을 때 하의 id: " + bottom.getClothId());
                styleResponseDtos.add(StyleRecommendResponseDto.builder()
                        .topId(top.getClothId())
                        .bottomId(bottom.getClothId())
                        .build());
            } else if(combination.size() == 2) {
                List<SmallCategory> innerCategoryList = toSmallCategory(combination.get(0).getType());
                Degree innerDegree = toDegree(combination.get(0).getThickness());
                System.out.println("2개 추천됐을 때 상의 CategoryList: " + innerCategoryList);
                List<Cloth> topList = clothRepository.findAllBySmallCategoryAndDegree(innerCategoryList, innerDegree);
                List<Cloth> sortedInnerList= topList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();
                Cloth inner = sortedInnerList.get(0);

                List<SmallCategory> outerCategoryList = toSmallCategory(combination.get(1).getType());
                Degree outerDegree = toDegree(combination.get(1).getThickness());
                System.out.println("2개 추천됐을 때 상의 CategoryList: " + outerCategoryList);
                List<Cloth> outerList = clothRepository.findAllBySmallCategoryAndDegree(outerCategoryList, outerDegree);
                List<Cloth> sortedOuterList= outerList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();
                //Cloth outer = sortedOuterList.get(0);
                Cloth outer = null;
                if (!sortedOuterList.isEmpty()) {
                    outer = sortedOuterList.get(0);
                }

                List<Cloth> bottomList = clothRepository.findAllByDegreeAndLargeCategory(getBottomDegree(feelTemp),LargeCategory.BOTTOM);
                List<Cloth> sortedBottomList = bottomList.stream()
                        .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .toList();

                SmallCategory outerCategory = outer.getSmallCategory();
                Cloth bottom = null;
                for (Cloth sortedBottom : sortedBottomList) {
                    SmallCategory bottomCategory = sortedBottom.getSmallCategory();

                    List<SmallCategory> notGoodBottoms = outerBottomNotGoodCombination.get(outerCategory);

                    if(notGoodBottoms == null || !notGoodBottoms.contains(bottomCategory)) {
                        bottom = sortedBottom;
                        break;
                    }
                }
                styleResponseDtos.add(StyleRecommendResponseDto.builder()
                        .outerId(outer.getClothId())
                        .topId(inner.getClothId())
                        .bottomId(bottom.getClothId())
                        .build());
            }
        }

        return styleResponseDtos;
    }

    /**
     *
     * @param range
     * @param feelTemp
     * @return feelTemp를 토대로 해당 구간에 있는 옷의 조합의 번호를 추출하여 List<Integer>로 반환
     */
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
        String parts[] = item.split("-");
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
        }
        return null;
    }

    private List<Cloth> getBottom(int feelTemp) {
        List<Degree> bottomDegree;

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

        List<Cloth> bottom = clothRepository.findAllByDegreeAndLargeCategory(bottomDegree, LargeCategory.BOTTOM);

        if (bottom == null || bottom.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 최근 착용 날짜를 기준으로 오름차순 정렬 (최근에 입지 않은 옷이 후순위)
        List<Cloth> sortedBottom = bottom.stream()
                .sorted(Comparator.comparing(Cloth::getWearAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        Random random = new Random();
        List<Cloth> selectedBottom = new ArrayList<>();

        // 2. 랜덤하게 최대 2개를 추출하여 반환한다.
        for (int i = 0; i < 2 && i < sortedBottom.size(); i++) {
            int randomIndex = random.nextInt(sortedBottom.size() - i);
            selectedBottom.add(sortedBottom.get(randomIndex));
            Collections.swap(sortedBottom, randomIndex, sortedBottom.size() - i - 1); // 뒤쪽으로 밀어서 다시 선택되지 않도록
        }

        return selectedBottom;
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

}

