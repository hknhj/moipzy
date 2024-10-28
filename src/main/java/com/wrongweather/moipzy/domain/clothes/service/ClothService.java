package com.wrongweather.moipzy.domain.clothes.service;

import com.wrongweather.moipzy.domain.clothImg.ClothImage;
import com.wrongweather.moipzy.domain.clothImg.ClothImageRepository;
import com.wrongweather.moipzy.domain.clothImg.service.ClothImgService;
import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ClothResponseDto getCloth(int clothId) {
        System.out.println(clothId);
        Cloth cloth = clothRepository.findByClothId(clothId).orElseThrow(() -> new RuntimeException());
        return ClothResponseDto.builder()
                .user(cloth.getUser())
                .cloth(cloth)
                .build();
    }

}
