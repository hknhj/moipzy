package com.wrongweather.moipzy.domain.clothes.service;

import com.wrongweather.moipzy.domain.clothes.Cloth;
import com.wrongweather.moipzy.domain.clothes.ClothRepository;
import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class ClothService {
    private final ClothRepository clothRepository;
    private final UserRepository userRepository;

    public ClothIdResponseDto registerCloth(ClothRegisterRequestDto clothRegisterRequestDto) {
        User user = userRepository.findByUserId(clothRegisterRequestDto.getUserId()).orElseThrow(() -> new RuntimeException());

        return ClothIdResponseDto.builder()
                .clothId(clothRepository.save(clothRegisterRequestDto.toEntity(user)).getClothId())
                .build();
    }
}
