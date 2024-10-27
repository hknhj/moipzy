package com.wrongweather.moipzy.domain.clothes.service;

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
        User user = userRepository.findByUserId(clothRegisterRequestDto.getUserId()).orElseThrow(() -> new RuntimeException());

        Cloth cloth = clothRepository.save(clothRegisterRequestDto.toEntity(user));
        clothImgService.uploadImage(clothImg, cloth);

        System.out.println(clothRegisterRequestDto.toString());

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
