package com.wrongweather.moipzy.domain.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final RestTemplate restTemplate;

    @Value("${weather.key}")
    String weatherServiceKey;

    @Value("${openWeather.api}")
    String openWeatherApiKey;

    // 서버 시작 시 실행
    @PostConstruct
    public void initDailyWeather() {
        log.info("Initializing daily weather information at server startup...");
        getWeather();
    }

    // 매일 05:00 실행
    @Scheduled(cron = "0 0 5 * * *")
    public void updateDailyWeather() {
        log.info("Updating daily weather information...");
        getWeather();
    }

    //todayMinTemp
    //todayMaxTemp
    //tomorrowMinTemp
    //tomorrowMaxTemp
    //redis에 저장
    public void getWeather() {
        String requestURL = "https://api.openweathermap.org/data/2.5/forecast" +
                "?lat=37" +
                "&lon=127" +
                "&units=metric" +
                "&appid="+openWeatherApiKey;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            List<Integer> tempList = getTemperatures(responseBody);

            log.info("Today min temp: " + tempList.get(0));
            log.info("Today max temp: " + tempList.get(1));
            log.info("Tomorrow min temp: " + tempList.get(2));
            log.info("Tomorrow max temp: " + tempList.get(3));
            // Extracting minTemp and maxTemp

            ValueOperations<String, Object> vop = redisTemplate.opsForValue();
            vop.set("todayMinTemp", tempList.get(0));
            vop.set("todayMaxTemp", tempList.get(1));
            vop.set("tomorrowMinTemp", tempList.get(2));
            vop.set("tomorrowMaxTemp", tempList.get(3));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Integer> getTemperatures(String responseBody) throws JsonProcessingException {
        List<Integer> temperatures = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON 데이터를 JsonNode로 변환
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode listNode = rootNode.get("list");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        int todayMin = Integer.MAX_VALUE;
        int todayMax = Integer.MIN_VALUE;
        int tomorrowMin = Integer.MAX_VALUE;
        int tomorrowMax = Integer.MIN_VALUE;

        // listNode의 각 항목을 순회
        Iterator<JsonNode> elements = listNode.elements();
        while (elements.hasNext()) {
            JsonNode item = elements.next();
            String dateTime = item.get("dt_txt").asText();
            LocalDate date = LocalDate.parse(dateTime.split(" ")[0]);

            int tempMin = (int) Math.round(item.get("main").get("temp_min").asDouble());
            int tempMax = (int) Math.round(item.get("main").get("temp_max").asDouble());

            if (date.equals(today)) {
                todayMin = Math.min(todayMin, tempMin);
                todayMax = Math.max(todayMax, tempMax);
            } else if (date.equals(tomorrow)) {
                tomorrowMin = Math.min(tomorrowMin, tempMin);
                tomorrowMax = Math.max(tomorrowMax, tempMax);
            }
        }

        temperatures.add(todayMin);
        temperatures.add(todayMax);
        temperatures.add(tomorrowMin);
        temperatures.add(tomorrowMax);

        return temperatures;
    }

    public String convertDate(String date) {
        LocalDate today = LocalDate.now();
        LocalDate resultDate;

        if (date.equalsIgnoreCase("today")) {
            resultDate = today;
        } else if (date.equalsIgnoreCase("tomorrow")) {
            resultDate = today.plusDays(1);
        } else {
            throw new IllegalArgumentException("오늘 또는 내일을 입력해주세요");
        }

        //날짜를 "yyyyddyy" 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return resultDate.format(formatter);
    }
}
