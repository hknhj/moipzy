package com.wrongweather.moipzy.domain.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.key}")
    String weatherServiceKey;

    //최저기온, 최고기온 순으로 return
    public List<Integer> getWeatherInfo(String date) throws Exception {
        //String resultDate = convertDate(date);
        String resultDate = convertDate("today");

        String requestURL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" +
                "serviceKey=" + weatherServiceKey +
                "&pageNo=1" +
                "&numOfRows=600" +
                "&dataType=JSON" +
                "&base_date=" + resultDate +
                "&base_time=" + "0500" +
                "&nx=55" +
                "&ny=127";
        System.out.println(resultDate);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String findDate = convertDate(date);

        try {
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            System.out.println(responseBody);
            System.out.println("날씨 정보 호출 성공!");

            // JSON 파싱 및 TMN, TMX 값 추출
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            int minTemperature = Integer.MAX_VALUE;
            int maxTemperature = Integer.MIN_VALUE;

            // JSON 배열 순회
            for (JsonNode item : items) {
                String fcstDate = item.path("fcstDate").asText(); // 예보 날짜
                String category = item.path("category").asText(); // 데이터 카테고리
                int value = item.path("fcstValue").asInt(); // 예보 값

                if (fcstDate.equals(findDate) && "TMP".equals(category)) { // 날짜와 카테고리가 TMP인 경우
                    minTemperature = Math.min(minTemperature, value);
                    maxTemperature = Math.max(maxTemperature, value);
                }
            }

            // 최저기온과 최고기온 출력
            if (minTemperature == Integer.MAX_VALUE || maxTemperature == Integer.MIN_VALUE) {
                System.out.println("최저기온 또는 최고기온 데이터를 찾을 수 없습니다.");
            } else {
                System.out.printf("날짜: %s\n최저기온: %d°C\n최고기온: %d°C\n", findDate, minTemperature, maxTemperature);
            }

            return Arrays.asList(minTemperature, maxTemperature);
        } catch(Exception e){
            System.out.println("날씨 정보 호출 실패");
            e.printStackTrace();
        }

        return null;
    }

    public String convertDate(String date) {
        LocalDate today = LocalDate.now();
        LocalDate resultDate;

        if ("today".equalsIgnoreCase(date)) {
            resultDate = today;
        } else if ("tomorrow".equalsIgnoreCase(date)) {
            resultDate = today.plusDays(1);
        } else {
            throw new IllegalArgumentException("오늘 또는 내일을 입력해주세요");
        }

        //날짜를 "yyyyddyy" 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return resultDate.format(formatter);
    }

    
}
