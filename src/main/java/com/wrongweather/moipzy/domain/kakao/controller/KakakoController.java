package com.wrongweather.moipzy.domain.kakao.controller;

import com.wrongweather.moipzy.domain.kakao.service.KaKaoService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
public class KakakoController {

    private final KaKaoService kaKaoService;

//    //카카오톡 오픈빌더로 리턴할 스킬 API
//    @PostMapping(value = "/kakaoTest", headers = "Accept=application/json")
//    public HashMap<String, Object> callAPI(@RequestBody Map<String, Object> params) {
//
//        HashMap<String, Object> resultJson = new HashMap<>();
//
//        try {
//
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(params);
//            System.out.println(jsonInString);
//
//            List<HashMap<String, Object>> outputs = new ArrayList<>();
//            HashMap<String, Object> template = new HashMap<>();
//            HashMap<String, Object> simpleText = new HashMap<>();
//            HashMap<String, Object> text = new HashMap<>();
//
//            text.put("text", "코딩32 발화리턴입니다.");
//            simpleText.put("simpleText", text);
//            outputs.add(simpleText);
//
//            template.put("outputs", outputs);
//
//            resultJson.put("version", "2.0");
//            resultJson.put("template", template);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return resultJson;
//    }
//
//    @PostMapping("/imgTest")
//    public Map<String, Object> handleKakaoRequest(@RequestBody Map<String, Object> requestBody) {
//        String top = "https://moipzy.shop/uploads/clothes/cbc88a6b5a704d37b6b60c2e67153c6e_니트1.webp";
//        String bottom = "https://moipzy.shop/uploads/clothes/5b4a6f6cbe5e4a7e9b684141de6216ac_슬랙스1.webp";
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonInString = mapper.writeValueAsString(requestBody);
//            System.out.println(jsonInString);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // JSON 응답 구조 생성
//        Map<String, Object> response = new HashMap<>();
//        response.put("version", "2.0");
//
//        // 템플릿 설정
//        Map<String, Object> template = new HashMap<>();
//        List<Map<String, Object>> outputs = new ArrayList<>();
//
//        // 카로셀 아이템들 생성
//        Map<String, Object> carousel = new HashMap<>();
//        carousel.put("type", "basicCard");
//
//        // 카로셀 아이템 목록
//        List<Map<String, Object>> items = new ArrayList<>();
//
//        // 상의 아이템
//        Map<String, Object> topItem = new HashMap<>();
//        topItem.put("title", "top");
//
//        // 상의 썸네일 설정 (imageUrl, fixedRatio)
//        Map<String, Object> topThumbnail = new HashMap<>();
//        topThumbnail.put("imageUrl", top);
//        topThumbnail.put("fixedRatio", true);  // 비율 고정
//        topItem.put("thumbnail", topThumbnail);
//
//        items.add(topItem);
//
//        // 하의 아이템
//        Map<String, Object> bottomItem = new HashMap<>();
//        bottomItem.put("title", "bottom");
//
//        // 하의 썸네일 설정 (imageUrl, fixedRatio)
//        Map<String, Object> bottomThumbnail = new HashMap<>();
//        bottomThumbnail.put("imageUrl", bottom);
//        bottomThumbnail.put("fixedRatio", true);  // 비율 고정
//        bottomItem.put("thumbnail", bottomThumbnail);
//
//        items.add(bottomItem);
//
//        // 카로셀 항목에 아이템들 추가
//        carousel.put("items", items);
//        outputs.add(Map.of("carousel", carousel));
//        template.put("outputs", outputs);
//        response.put("template", template);
//
//        return response;
//    }

    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody Map<String, Object> requestBody) {
        List<String> utteranceAndKakaoId = getUtteranceAndKakaoId(requestBody);
        return kaKaoService.getStyleRecommends(utteranceAndKakaoId.get(0), utteranceAndKakaoId.get(1));
    }

    @PostMapping("/weather")
    public Map<String, Object> getWeather(@RequestBody Map<String, Object> requestBody) {
        String userId = getUtteranceAndKakaoId(requestBody).get(1);
        return kaKaoService.getWeather(userId);
    }

    @PostMapping("/calendar")
    public Map<String, Object> getCalendar(@RequestBody Map<String, Object> requestBody) {
        String userId = getUtteranceAndKakaoId(requestBody).get(1);
        return kaKaoService.getEvents(userId);
    }

    @PostMapping("/fallback")
    public Map<String, Object> getFallback(@RequestBody Map<String, Object> requestBody) {
        List<String> utteranceAndKakaoId = getUtteranceAndKakaoId(requestBody);
        return kaKaoService.fallbackBlock(utteranceAndKakaoId.get(0), utteranceAndKakaoId.get(1));
    }

    // requestBody에서 utterance, userId 추출
    List<String> getUtteranceAndKakaoId(Map<String, Object> requestBody) {
        JSONObject jsonObject = new JSONObject(requestBody);
        String utterance = jsonObject.getJSONObject("userRequest").getString("utterance");
        String userId = jsonObject.getJSONObject("userRequest").getJSONObject("user").getString("id");

        return Arrays.asList(utterance, userId);
    }
}
