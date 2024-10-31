package com.wrongweather.moipzy.domain.clothes.service;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.ClothImageRepository;
import com.wrongweather.moipzy.domain.clothImg.service.ClothImgService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.category.LargeCategory;
import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClothService {
    private final ClothRepository clothRepository;
    private final UserRepository userRepository;
    private final ClothImgService clothImgService;

    @Transactional
    public ClothIdResponseDto registerCloth(MultipartFile clothImg, ClothRegisterRequestDto clothRegisterRequestDto) {
        //유저 정보를 먼저 불러오기
        User user = userRepository.findByUserId(clothRegisterRequestDto.getUserId()).orElseThrow(() -> new RuntimeException());

        //사진을 저장하기
        ClothImage clothImage = clothImgService.uploadImage(clothImg);

        //옷을 저장하기
        Cloth cloth = clothRepository.save(clothRegisterRequestDto.toEntity(user, clothImage));

        return ClothIdResponseDto.builder()
                .clothId(cloth.getClothId())
                .build();
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

    public List<ClothResponseDto> getAllOuter(int userId, String largeCategory) throws RuntimeException {
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
}
