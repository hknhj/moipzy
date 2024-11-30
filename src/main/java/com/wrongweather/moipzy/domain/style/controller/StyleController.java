package com.wrongweather.moipzy.domain.style.controller;

import com.wrongweather.moipzy.domain.chatGPT.dto.OutfitResponse;
import com.wrongweather.moipzy.domain.style.dto.StyleFeedbackRequestDto;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.dto.StyleResponseDto;
import com.wrongweather.moipzy.domain.style.dto.StyleUploadRequestDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("moipzy/style")
@RequiredArgsConstructor
@Slf4j
public class StyleController {

    private final StyleService styleService;


    // 옷 추천 controller
    @GetMapping("/recommend")
    public List<StyleRecommendResponseDto> recommendByHighLow(@RequestParam int userId, @RequestParam int highTemp, @RequestParam int lowTemp) {
        return styleService.recommend(userId, highTemp, lowTemp);
    }

    // 옷차림 등록 controller
    @PostMapping
    public ResponseEntity<String> uploadStyle(@RequestBody StyleUploadRequestDto styleUploadRequestDto) {
        int uploadedStyleId = styleService.uploadStyle(styleUploadRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("옷차림 등록 완료. Id: " + uploadedStyleId);
    }

    // 옷차림 get controller
    @GetMapping("/{userId}")
    public StyleResponseDto getStyle(@PathVariable int userId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return styleService.getStyle(userId, date);
    }

    // 피드백 controller
    @PatchMapping("/feedback")
    public ResponseEntity<String> styleFeedback(@RequestBody StyleFeedbackRequestDto requestDto) {
        try {
            int styleId = styleService.updateTemperature(requestDto);
            return ResponseEntity.ok("style feedback completed successfully. Id: " + styleId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}