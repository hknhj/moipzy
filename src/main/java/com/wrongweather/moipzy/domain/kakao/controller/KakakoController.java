package com.wrongweather.moipzy.domain.kakao.controller;

import com.wrongweather.moipzy.domain.kakao.service.KaKaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
@Slf4j
public class KakakoController {

    private final KaKaoService kaKaoService;

    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody Map<String, Object> requestBody) {
        List<String> utteranceAndKakaoId = getUtteranceAndKakaoId(requestBody);
        log.info("kakaoId: " + utteranceAndKakaoId.get(1) + " recommed requested");
        return kaKaoService.getStyleRecommendsInRedis(utteranceAndKakaoId.get(0), utteranceAndKakaoId.get(1));
    }

    @PostMapping("/weather")
    public Map<String, Object> getWeather(@RequestBody Map<String, Object> requestBody) {
        String userId = getUtteranceAndKakaoId(requestBody).get(1);
        log.info("kakaoId: " + userId + " getWeather requested");
        return kaKaoService.getWeather(userId);
    }

    @PostMapping("/calendar")
    public Map<String, Object> getCalendar(@RequestBody Map<String, Object> requestBody) {
        String userId = getUtteranceAndKakaoId(requestBody).get(1);
        log.info("kakaoId: " + userId + " getCalendar requested");
        return kaKaoService.getEvents(userId);
    }

    @PostMapping("/information")
    public Map<String, Object> getInformation(@RequestBody Map<String, Object> requestBody) {
        List<String> utteranceAndKakaoId = getUtteranceAndKakaoId(requestBody);
        log.info("kakaoId: " + utteranceAndKakaoId.get(1) + " getInformation requested");
        return kaKaoService.getInformation(utteranceAndKakaoId.get(0), utteranceAndKakaoId.get(1));
    }

    @PostMapping("/fallback")
    public Map<String, Object> getFallback(@RequestBody Map<String, Object> requestBody) {
        List<String> utteranceAndKakaoId = getUtteranceAndKakaoId(requestBody);
        log.info("kakaoId: " + utteranceAndKakaoId.get(1) + " fallback requested");
        return kaKaoService.fallbackBlock(utteranceAndKakaoId.get(0), utteranceAndKakaoId.get(1));
    }

    @PostMapping("/outfit")
    public Map<String, Object> getOutfit(@RequestBody Map<String, Object> requestBody) {
        List<String> utterandAndKakaoId = getUtteranceAndKakaoId(requestBody);
        log.info("kakaoId: " + utterandAndKakaoId.get(1) + " getOutfit requested");
        return kaKaoService.getOutfit(utterandAndKakaoId.get(0), utterandAndKakaoId.get(1));
    }

    // requestBody에서 utterance, userId 추출
    List<String> getUtteranceAndKakaoId(Map<String, Object> requestBody) {
        JSONObject jsonObject = new JSONObject(requestBody);
        String utterance = jsonObject.getJSONObject("userRequest").getString("utterance");
        String userId = jsonObject.getJSONObject("userRequest").getJSONObject("user").getString("id");

        return Arrays.asList(utterance, userId);
    }
}
