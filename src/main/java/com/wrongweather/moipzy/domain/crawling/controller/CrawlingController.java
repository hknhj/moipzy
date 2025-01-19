package com.wrongweather.moipzy.domain.crawling.controller;

import com.wrongweather.moipzy.domain.crawling.dto.CrawlingResponseDto;
import com.wrongweather.moipzy.domain.crawling.exception.CrawlingFailedException;
import com.wrongweather.moipzy.domain.crawling.service.CrawlingService;
import com.wrongweather.moipzy.domain.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moipzy/crawling")
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;

    @PostMapping("/musinsa")
    public CrawlingResponseDto crawlMusinsa(@RequestParam String url) {
        return crawlingService.crawlMusinsa(url);
    }

    @ExceptionHandler(CrawlingFailedException.class)
    public ResponseEntity<ErrorResponse> crawlingFailedException(CrawlingFailedException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}
