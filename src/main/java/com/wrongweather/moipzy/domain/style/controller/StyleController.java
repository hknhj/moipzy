package com.wrongweather.moipzy.domain.style.controller;

import com.wrongweather.moipzy.domain.style.dto.StyleUploadRequestDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("moipzy/style")
@RequiredArgsConstructor
@Slf4j
public class StyleController {

    private final StyleService styleService;

    @GetMapping("/recommend")
    public String recommendList(@RequestParam int highTemp, @RequestParam int lowTemp) {
        String recommended = styleService.recommendTest(highTemp, lowTemp);
        return recommended;
    }

    @PostMapping
    public ResponseEntity<String> uploadStyle(@RequestBody StyleUploadRequestDto styleUploadRequestDto) {
        int uploadedStyleId = styleService.uploadStyle(styleUploadRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("옷차림 등록 완료. Id: " + uploadedStyleId);
    }
}