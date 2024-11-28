package com.wrongweather.moipzy.domain.calendar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

    private final TokenRepository tokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, List<Map<String, String>>> getEventsByDate(String accessToken, String date) throws IOException {

        // Map to store events for the requested date with time
        Map<String, List<Map<String, String>>> eventMap = new HashMap<>();

        //Formatter for the input date and UTC time conversion
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        //Parse the input date
        LocalDate requestedDate = LocalDate.parse(date, inputFormatter);

        // Generate start and end times in UTC
        String startOfDay = requestedDate.atStartOfDay(ZoneOffset.UTC).format(utcFormatter);
        String endOfDay = requestedDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(utcFormatter);

        // Get date's url in UTC format
        String todayUrl = getUrl(startOfDay, endOfDay);

        // Authorization 헤더에 Access Token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // request를 위한 HttpEntity
        HttpEntity<String> request = new HttpEntity<>(headers);

        // Google Calendar API에 오늘 event에 대한 GET 요청
        ResponseEntity<String> response = restTemplate.exchange(todayUrl, HttpMethod.GET, request, String.class);

        // 응답을 반환 (JSON 형식의 일정 데이터)
        String responseBody =  response.getBody();
        List<Map<String, String>> events = getEventsWithTime(responseBody);

        eventMap.put(date, events);

        return eventMap;
    }

    public Map<LocalDate, List<Map<String, String>>> getEventTest(int userId, LocalDate date) throws IOException {

        // Map to store events for the requested date with time
        Map<LocalDate, List<Map<String, String>>> eventMap = new HashMap<>();

        // Formatter for UTC time conversion
        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        // Generate start and end times in UTC
        String startOfDay = date.atStartOfDay(ZoneOffset.UTC).format(utcFormatter);
        String endOfDay = date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(utcFormatter);

        // Get date's URL in UTC format
        String todayUrl = getUrl(startOfDay, endOfDay);

        String accessToken = tokenRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException()).getAccessToken();
        System.out.println(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(todayUrl, HttpMethod.GET, request, String.class);

        String responseBody =  response.getBody();
        List<Map<String, String>> events = getEventsWithTime(responseBody);

        eventMap.put(date, events);

        System.out.println(eventMap);

        return eventMap;
    }

    private String getUrl(String startOfDay, String endOfDay) {
        return "https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMin="
                + startOfDay + "&timeMax=" + endOfDay + "&singleEvents=true&orderBy=startTime&maxResults=10";
    }

    private List<Map<String, String>> getEventsWithTime(String res) throws IOException {

        List<Map<String, String>> events = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(res);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                String summary = item.path("summary").asText();
                String start = item.path("start").path("dateTime").asText();
                String end = item.path("end").path("dateTime").asText();

                // Store event information in a map
                Map<String, String> eventDetails = new HashMap<>();
                eventDetails.put("summary", summary);
                eventDetails.put("startTime", start);
                eventDetails.put("endTime", end);

                events.add(eventDetails);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return events;
    }
}
