package com.wrongweather.moipzy.domain.clothes.service;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.service.ClothImgService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.Degree;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClothService {
    private final ClothRepository clothRepository;
    private final UserRepository userRepository;
    private final ClothImgService clothImgService;

    List<SmallCategory> lightOuterList = Arrays.asList(SmallCategory.CARDIGAN, SmallCategory.DENIM_JACKET, SmallCategory.BLOUSON, SmallCategory.BLAZER, SmallCategory.HOODED);
    List<SmallCategory> mediumOuterList = Arrays.asList(SmallCategory.MA1, SmallCategory.STADIUM_JACKET, SmallCategory.LEATHER_JACKET, SmallCategory.JACKET);
    List<SmallCategory> sweaterList = Arrays.asList(SmallCategory.SWEAT_SHIRT, SmallCategory.HOODIE, SmallCategory.KNIT);

    private final int INF_HIGH_TEMPERATURE = 70;
    private final int INF_LOW_TEMPERATURE = -70;

    @Transactional
    public int registerCloth(MultipartFile clothImg, ClothRegisterRequestDto clothRegisterRequestDto) {
        //유저 정보를 먼저 불러오기
        User user = userRepository.findByUserId(clothRegisterRequestDto.getUserId()).orElseThrow(() -> new RuntimeException());

        //사진을 저장하기
        ClothImage clothImage = clothImgService.uploadImage(clothImg);

        //옷 추출
        Cloth cloth = clothRegisterRequestDto.toEntity(user, clothImage);

        //옷 온도 구간 설정
        if (!cloth.getSmallCategory().equals(SmallCategory.D_SHIRT)) {
            List<Integer> tempList = getLowHighTemp(cloth);

            if (tempList.get(0) != null) {
                cloth.setLowTemperature(tempList.get(0));
            }
            if (tempList.get(1) != null) {
                cloth.setHighTemperature(tempList.get(1));
            }

            List<Integer> soloTempList = getSoloTopLowHighTemp(cloth);

            if (soloTempList.get(0) != null) {
                cloth.setSoloLowTemperature(soloTempList.get(0));
            }
            if (soloTempList.get(1) != null) {
                cloth.setSoloHighTemperature(soloTempList.get(1));
            }
        }
        else {
            List<Integer> tempList = getShirtOuterLowHighTemp(cloth);
            if (tempList.get(0) != null) {
                cloth.setLowTemperature(tempList.get(0));
            }
            if (tempList.get(1) != null) {
                cloth.setHighTemperature(tempList.get(1));
            }

            List<Integer> soloTempList = getSoloShirtTopLowHighTemp(cloth);
            if (soloTempList.get(0) != null) {
                cloth.setSoloLowTemperature(soloTempList.get(0));
            }
            if (soloTempList.get(1) != null) {
                cloth.setSoloHighTemperature(soloTempList.get(1));
            }
        }

        return clothRepository.save(cloth).getClothId();
    }

    public ClothResponseDto getCloth(int userId, int clothId) throws RuntimeException {
        //System.out.println(clothId);
        Cloth cloth = clothRepository.findByClothId(clothId).orElseThrow(() -> new RuntimeException());
        if (userId != cloth.getUser().getUserId()) {
            throw new RuntimeException();
        }
        System.out.println(cloth.getClothImg().getImgUrl());
        return ClothResponseDto.builder()
                .user(cloth.getUser())
                .cloth(cloth)
                .build();
    }

    public List<ClothResponseDto> getAllClothes(int userId) {
        List<Cloth> clothList = clothRepository.findAllByUser_UserId(userId);
        List<ClothResponseDto> clothResponseDtoList = new ArrayList<>();
        for (Cloth cloth : clothList) {
            clothResponseDtoList.add(ClothResponseDto.builder()
                    .user(cloth.getUser())
                    .cloth(cloth)
                    .build());
        }

        return clothResponseDtoList;
    }

    public List<ClothResponseDto> getAllByLargeCategory(int userId, String largeCategory) throws RuntimeException {
        LargeCategory temp;
        if (largeCategory.equals("outer")) {
            temp=LargeCategory.OUTER;
        } else if (largeCategory.equals("top")) {
            temp=LargeCategory.TOP;
        } else if (largeCategory.equals("bottom")) {
            temp = LargeCategory.BOTTOM;
        } else {
            throw new RuntimeException();
        }
        List<Cloth> clothList = clothRepository.findAllByLargeCategory(temp);
        List<ClothResponseDto> clothResponseDtoList = new ArrayList<>();
        for (Cloth cloth : clothList) {
            clothResponseDtoList.add(ClothResponseDto.builder()
                    .user(cloth.getUser())
                    .cloth(cloth)
                    .build());
        }

        return clothResponseDtoList;
    }

    private List<Integer> getLowHighTemp(Cloth cloth) {
        //1번 인덱스가 low temp, 2번 인덱스가 high temp
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getLargeCategory().equals(LargeCategory.OUTER)) {
            tempList = getOuterLowHighTemp(cloth);
        } else if (cloth.getLargeCategory().equals(LargeCategory.TOP)) {
            tempList = getTopLowHighTemp(cloth);
        } else if (cloth.getLargeCategory().equals(LargeCategory.BOTTOM)) {
            tempList = getBottomLowHighTemp(cloth);
        }
        return tempList;
    }

    private List<Integer> getOuterLowHighTemp(Cloth cloth) {

        //1번 인덱스가 low temp, 2번 인덱스가 high temp
        List<Integer> tempList = Arrays.asList(null, null);

        if (lightOuterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(17, 19);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(13, 16);
            }
        }
        else if (mediumOuterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(11, 14);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(6, 10);
            }
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.COAT)) {
            if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(9, 12);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(INF_LOW_TEMPERATURE, 8);
            }
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.PADDING)) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(9, 12);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(5, 8);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(INF_LOW_TEMPERATURE, 4);
            }
        }
        return tempList;
    }

    private List<Integer> getTopLowHighTemp(Cloth cloth) {
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getSmallCategory().equals(SmallCategory.T_SHIRT)) {
            tempList = Arrays.asList(24, INF_HIGH_TEMPERATURE);
        } else if (cloth.getSmallCategory().equals(SmallCategory.POLO_SHIRT)) {
            tempList = Arrays.asList(24, INF_HIGH_TEMPERATURE);
        } else if (cloth.getSmallCategory().equals(SmallCategory.LONG_SLEEVE)) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(20, 23);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(15, 19);
            }
        } else if (sweaterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(13, 16);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(INF_LOW_TEMPERATURE, 12);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(INF_LOW_TEMPERATURE, 8);
            }
        }
        return tempList;
    }

    private List<Integer> getBottomLowHighTemp(Cloth cloth) {
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getSmallCategory().equals(SmallCategory.SHORTS)) {
            tempList = Arrays.asList(28, INF_HIGH_TEMPERATURE);
        }
        else if (cloth.getDegree().equals(Degree.THIN)) {
            tempList = Arrays.asList(24, INF_HIGH_TEMPERATURE);
        }
        else if (cloth.getDegree().equals(Degree.LTHIN)) {
            tempList = Arrays.asList(17, 27);
        }
        else if (cloth.getDegree().equals(Degree.NORMAL)) {
            tempList = Arrays.asList(INF_LOW_TEMPERATURE, 23);
        }
        else if (cloth.getDegree().equals(Degree.LTHICK)) {
            tempList = Arrays.asList(INF_LOW_TEMPERATURE, 16);
        }
        else if (cloth.getDegree().equals(Degree.THICK)) {
            tempList = Arrays.asList(INF_LOW_TEMPERATURE, 8);
        }
        return tempList;
    }

    // 아우터 없이 상의만 입을 때
    private List<Integer> getSoloTopLowHighTemp(Cloth cloth) {
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getSmallCategory().equals(SmallCategory.T_SHIRT)) {
            tempList = Arrays.asList(26, INF_HIGH_TEMPERATURE);
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.POLO_SHIRT)) {
            tempList = Arrays.asList(24, 30);
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.LONG_SLEEVE)) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(24, 27);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(20, 23);
            }
        } else if (sweaterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(17, 19);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(13, 16);
            }
        }
        return tempList;
    }

    // 셔츠를 아우터로 입을 때 (solo_high_temp, solo_low_temp)
    private List<Integer> getShirtOuterLowHighTemp(Cloth cloth) {
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getDegree().equals(Degree.THIN)) {
            tempList = Arrays.asList(24, 27);
        } else if (cloth.getDegree().equals(Degree.NORMAL)) {
            tempList = Arrays.asList(17, 23);
        } else if (cloth.getDegree().equals(Degree.THICK)) {
            tempList = Arrays.asList(13, 16);
        }
        return tempList;
    }

    private List<Integer> getSoloShirtTopLowHighTemp(Cloth cloth) {
        List<Integer> tempList = Arrays.asList(null, null);
        if (cloth.getDegree().equals(Degree.NORMAL)) {
            tempList = Arrays.asList(9, 16);
        }
        return tempList;
    }
}
