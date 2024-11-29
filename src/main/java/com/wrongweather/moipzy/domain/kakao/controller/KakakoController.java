package com.wrongweather.moipzy.domain.kakao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class KakakoController {

    //카카오톡 오픈빌더로 리턴할 스킬 API
    @PostMapping(value = "/kakaoTest", headers = "Accept=application/json")
    public HashMap<String, Object> callAPI(@RequestBody Map<String, Object> params) {

        HashMap<String, Object> resultJson = new HashMap<>();

        try{

            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(params);
            System.out.println(jsonInString);

            List<HashMap<String,Object>> outputs = new ArrayList<>();
            HashMap<String,Object> template = new HashMap<>();
            HashMap<String, Object> simpleText = new HashMap<>();
            HashMap<String, Object> text = new HashMap<>();

            text.put("text","코딩32 발화리턴입니다.");
            simpleText.put("simpleText",text);
            outputs.add(simpleText);

            template.put("outputs",outputs);

            resultJson.put("version","2.0");
            resultJson.put("template",template);

        } catch (Exception e){
            e.printStackTrace();
        }
        return resultJson;
    }

    @PostMapping("/imgTest")
    public Map<String, Object> handleKakaoRequest(@RequestBody Map<String, Object> requestBody) {
        String top = "https://moipzy.shop/uploads/clothes/cbc88a6b5a704d37b6b60c2e67153c6e_니트1.webp";
        String bottom = "https://moipzy.shop/uploads/clothes/5b4a6f6cbe5e4a7e9b684141de6216ac_슬랙스1.webp";

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();

        // 카로셀 아이템들 생성
        Map<String, Object> carousel = new HashMap<>();
        carousel.put("type", "basicCard");

        // 카로셀 아이템 목록
        List<Map<String, Object>> items = new ArrayList<>();

        // 상의 아이템
        Map<String, Object> topItem = new HashMap<>();
        topItem.put("title", "상의 추천");
        topItem.put("description", "내일 날씨에 적합한 상의입니다.");
        topItem.put("imageUrl", top); // 상의 이미지 URL만 추가
//        topItem.put("buttons", List.of(
//                Map.of("action", "webLink", "label", "자세히 보기", "webLinkUrl", "https://your-service.com")
//        ));
        items.add(topItem);

        // 하의 아이템
        Map<String, Object> bottomItem = new HashMap<>();
        bottomItem.put("title", "하의 추천");
        bottomItem.put("description", "내일 날씨에 적합한 하의입니다.");
        bottomItem.put("imageUrl", bottom); // 하의 이미지 URL만 추가
//        bottomItem.put("buttons", List.of(
//                Map.of("action", "webLink", "label", "자세히 보기", "webLinkUrl", "https://your-service.com")
//        ));
        items.add(bottomItem);

        // 카로셀 항목에 아이템들 추가
        carousel.put("items", items);
        outputs.add(Map.of("carousel", carousel));
        template.put("outputs", outputs);
        response.put("template", template);

        return response;
    }
}
