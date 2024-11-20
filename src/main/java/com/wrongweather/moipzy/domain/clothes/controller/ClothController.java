package com.wrongweather.moipzy.domain.clothes.controller;

import com.wrongweather.moipzy.domain.clothes.dto.ClothRegisterRequestDto;
import com.wrongweather.moipzy.domain.clothes.dto.ClothResponseDto;
import com.wrongweather.moipzy.domain.clothes.service.ClothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("moipzy/clothes")
@RequiredArgsConstructor
@Slf4j
public class ClothController {

    private final ClothService clothService;

    /**
     *
     * @param clothImg
     * @param clothRegisterRequestDto
     * @return 옷 정보 json, 옷 사진파일을 받아 db에 저장
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCloth(@RequestPart("clothImg") MultipartFile clothImg, @RequestPart("clothData") ClothRegisterRequestDto clothRegisterRequestDto) {
        int clothId = clothService.registerCloth(clothImg, clothRegisterRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("옷 등록 완료. Id : " + clothId);
    }

    /**
     *
     * @param userId
     * @param clothId
     * @return userId, clothId를 이용하여 옷 개별 조회
     */
    @GetMapping("/{userId}/{clothId}")
    public ClothResponseDto getCloth(@PathVariable("userId") int userId, @PathVariable("clothId") int clothId) {
        return clothService.getCloth(userId, clothId);
    }

    /**
     *
     * @param userId
     * @return 유저가 가진 모든 옷 조회
     */
    @GetMapping("/{userId}")
    public List<ClothResponseDto> getAllClothes(@PathVariable("userId") int userId) {
        return clothService.getAllClothes(userId);
    }

    /**
     *
     * @param userId
     * @param largeCategory
     * @return userId와 largeCategory를 사용하여 카테고리별로 옷 리스트를 조회
     */
    @GetMapping("/largeCategory/{userId}")
    public List<ClothResponseDto> getAllOuter(@PathVariable("userId") int userId, @RequestParam String largeCategory) {
        return clothService.getAllByLargeCategory(userId, largeCategory);
    }
}
