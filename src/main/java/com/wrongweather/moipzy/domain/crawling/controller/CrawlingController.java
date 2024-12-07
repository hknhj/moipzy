package com.wrongweather.moipzy.domain.crawling.controller;

import com.wrongweather.moipzy.domain.crawling.dto.CrawlingResponseDto;
import com.wrongweather.moipzy.domain.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
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
}
