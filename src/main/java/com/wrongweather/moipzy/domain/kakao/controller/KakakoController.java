package com.wrongweather.moipzy.domain.kakao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class KakakoController {

    //카카오톡 오픈빌더로 리턴할 스킬 API
    @PostMapping(value = "/kakaoTest", headers = "Accept=application/json")
    public String callAPI(@RequestBody Map<String, Object> params) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(params);
            int x = 0;
            System.out.println(jsonInString);

            return "정상적으로 작동되었습니다";
        } catch (Exception e) {
            System.out.println("에러 발생 " + e);
            return "에러발생";
        }
    }
}
