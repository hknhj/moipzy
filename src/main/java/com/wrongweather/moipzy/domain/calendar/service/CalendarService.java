package com.wrongweather.moipzy.domain.calendar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getTomorrowEvents(String accessToken) throws GeneralSecurityException, IOException {

        String tomorrowStart = "2024-11-14T00:00:00Z";
        String tomorrowEnd = "2024-11-14T23:59:59Z";

        // Google Calendar API 요청 URL
        String url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin="
                + tomorrowStart + "&timeMax=" + tomorrowEnd + "&singleEvents=true&orderBy=startTime&maxResults=10";

        // Authorization 헤더에 Access Token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        // Google Calendar API에 GET 요청
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        // 응답을 반환 (JSON 형식의 일정 데이터)
        String res =  response.getBody();

        List<String> summaries = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(res);

        // "items" 배열 추출
        JsonNode itemsNode = rootNode.path("items");

        // 각 이벤트의 "summary" 필드 추출
        for (JsonNode item : itemsNode) {
            String summary = item.path("summary").asText();
            summaries.add(summary);
        }

        return summaries;
    }

}
