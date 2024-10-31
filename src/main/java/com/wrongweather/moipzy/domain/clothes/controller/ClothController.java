package com.wrongweather.moipzy.domain.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.clothes.dto.ClothIdResponseDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.clothes.service.ClothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("moipzy/clothes")
@RequiredArgsConstructor
@Slf4j
public class ClothController {

    private final ClothService clothService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCloth(@RequestPart("clothImg") MultipartFile clothImg, @RequestPart("clothData") ClothRegisterRequestDto clothRegisterRequestDto) {
        ClothIdResponseDto clothIdResponseDto = clothService.registerCloth(clothImg, clothRegisterRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("옷 등록 완료. Id : " + clothIdResponseDto.getClothId());
    }

    @GetMapping("/{userId}/{clothId}")
    public ClothResponseDto getCloth(@PathVariable("userId") int userId, @PathVariable("clothId") int clothId) {
        return clothService.getCloth(userId, clothId);
    }

    @GetMapping("/{userId}")
    public List<ClothResponseDto> getAllClothes(@PathVariable("userId") int userId) {
        return clothService.getAllClothes(userId);
    }

    @GetMapping("/largeCategory/{userId}")
    public List<ClothResponseDto> getAllOuter(@PathVariable("userId") int userId, @RequestParam String largeCategory) {
        return clothService.getAllOuter(userId, largeCategory);
    }
}
