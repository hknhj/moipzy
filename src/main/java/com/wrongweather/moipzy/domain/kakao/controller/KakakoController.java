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
}
