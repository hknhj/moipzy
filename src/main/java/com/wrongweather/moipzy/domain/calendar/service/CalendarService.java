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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, List<String>> getTodayAndTomorrowEvents(String accessToken) throws GeneralSecurityException, IOException {

        Map<String, List<String>> eventMap = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        // Get today's date in UTC format
        String todayStart = LocalDate.now().atStartOfDay(ZoneOffset.UTC).format(formatter);
        String todayEnd = LocalDate.now().atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(formatter);
        String todayUrl = getUrl(todayStart, todayEnd);

        // Get tomorrow's date in UTC format
        String tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).format(formatter);
        String tomorrowEnd = LocalDate.now().plusDays(1).atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(formatter);
        String tomorrowUrl = getUrl(tomorrowStart, tomorrowEnd);

        // Authorization 헤더에 Access Token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // request를 위한 HttpEntity
        HttpEntity<String> request = new HttpEntity<>(headers);

        // Google Calendar API에 오늘 event에 대한 GET 요청
        ResponseEntity<String> todayResponse = restTemplate.exchange(todayUrl, HttpMethod.GET, request, String.class);

        // 응답을 반환 (JSON 형식의 일정 데이터)
        String todayRes =  todayResponse.getBody();
        List<String> todayEvents = getEvents(todayRes);

        // Google Calendar API에 내일 event에 대한 GET 요청
        ResponseEntity<String> tomorrowResponse = restTemplate.exchange(tomorrowUrl, HttpMethod.GET, request, String.class);

        // 응답을 반환 (JSON 형식의 일정 데이터)
        String tomorrowRes =  tomorrowResponse.getBody();
        List<String> tomorrowEvents = getEvents(tomorrowRes);

        eventMap.put("today", todayEvents);
        eventMap.put("tomorrow", tomorrowEvents);

        return eventMap;
    }

    private String getUrl(String startOfDay, String endOfDay) {
        return "https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin="
                + startOfDay + "&timeMax=" + endOfDay + "&singleEvents=true&orderBy=startTime&maxResults=10";
    }

    private List<String> getEvents(String res) throws IOException {

        List<String> summaries = new ArrayList<>();

        JsonNode node = mapper.readTree(res);

        // "items" 배열 추출
        JsonNode todayItemsNode = node.path("items");

        // 각 이벤트의 "summary" 필드 추출
        for (JsonNode item : todayItemsNode) {
            String summary = item.path("summary").asText();
            summaries.add(summary);
        }

        return summaries;
    }
}
