package com.wrongweather.moipzy.domain.clothes.service;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.ClothImageRepository;
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

    @Transactional
    public int registerCloth(MultipartFile clothImg, ClothRegisterRequestDto clothRegisterRequestDto) {
        //유저 정보를 먼저 불러오기
        User user = userRepository.findByUserId(clothRegisterRequestDto.getUserId()).orElseThrow(() -> new RuntimeException());

        //사진을 저장하기
        ClothImage clothImage = clothImgService.uploadImage(clothImg);

        //옷 추출
        Cloth cloth = clothRegisterRequestDto.toEntity(user, clothImage);

        //옷 온도 구간 설정
        List<Integer> tempList = getLowHighTemp(cloth);
        cloth.setLowTemperature(tempList.get(0));
        cloth.setHighTemperature(tempList.get(1));

        List<Integer> soloTempList = getSoloLowHighTemp(cloth);
        cloth.setSoloLowTemperature(soloTempList.get(0));
        cloth.setSoloHighTemperature(soloTempList.get(1));

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
        List<Integer> tempList = null;
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
        List<Integer> tempList = null;

        if (cloth.getSmallCategory().equals(SmallCategory.D_SHIRT)) { //셔츠 아우터로 입을 때
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(24, 27);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(17, 23);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(13, 16);
            }
        }
        else if (lightOuterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(17, 19);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(13, 16);
            }
        }
        else if (mediumOuterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(9, 12);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(5, 8);
            }
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.COAT)) {
            if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(9, 12);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(5, 8);
            }
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.PADDING)) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(9, 12);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(5, 8);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(null, 4);
            }
        }
        return tempList;
    }

    private List<Integer> getTopLowHighTemp(Cloth cloth) {
        List<Integer> tempList = null;
        if (cloth.getSmallCategory().equals(SmallCategory.T_SHIRT)) {
            tempList = Arrays.asList(24, null);
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.D_SHIRT)) {
            if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(9, 16);
            }
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.LONG_SLEEVE)) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(20, 23);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(15, 19);
            }
        }
        else if (sweaterList.contains(cloth.getSmallCategory())) {
            if (cloth.getDegree().equals(Degree.THIN)) {
                tempList = Arrays.asList(13, 16);
            } else if (cloth.getDegree().equals(Degree.NORMAL)) {
                tempList = Arrays.asList(null, 12);
            } else if (cloth.getDegree().equals(Degree.THICK)) {
                tempList = Arrays.asList(null, 8);
            }
        }
        return tempList;
    }

    private List<Integer> getBottomLowHighTemp(Cloth cloth) {
        List<Integer> tempList = null;
        if (cloth.getSmallCategory().equals(SmallCategory.SHORTS)) {
            tempList = Arrays.asList(28, null);
        }
        else if (cloth.getDegree().equals(Degree.THIN)) {
            tempList = Arrays.asList(24, null);
        }
        else if (cloth.getDegree().equals(Degree.LTHIN)) {
            tempList = Arrays.asList(17, 27);
        }
        else if (cloth.getDegree().equals(Degree.NORMAL)) {
            tempList = Arrays.asList(null, 23);
        }
        else if (cloth.getDegree().equals(Degree.LTHICK)) {
            tempList = Arrays.asList(null, 16);
        }
        else if (cloth.getDegree().equals(Degree.THICK)) {
            tempList = Arrays.asList(null, 8);
        }
        return tempList;
    }

    private List<Integer> getSoloLowHighTemp(Cloth cloth) {
        List<Integer> tempList = null;
        if (cloth.getSmallCategory().equals(SmallCategory.T_SHIRT)) {
            tempList = Arrays.asList(25, null);
        }
        else if (cloth.getSmallCategory().equals(SmallCategory.POLO_SHIRT)) {
            tempList = Arrays.asList(24, null);
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
}
