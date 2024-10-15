package com.wrongweather.moipzy.domain.clothes.controller;

import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.clothes.service.ClothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("moipzy/clothes")
@RequiredArgsConstructor
@Slf4j
public class ClothController {

    private final ClothService clothService;

    @PostMapping
    public ClothIdResponseDto clothRegister(@Validated @RequestBody ClothRegisterRequestDto clothRegisterRequestDto) {
        return clothService.registerCloth(clothRegisterRequestDto);
    }

    @GetMapping("/{clothId}")
    public ClothResponseDto getCloth(@PathVariable("clothId") int clothId) {
        return clothService.getCloth(clothId);
    }
}
