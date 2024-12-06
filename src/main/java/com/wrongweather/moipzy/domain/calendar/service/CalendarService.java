package com.wrongweather.moipzy.domain.calendar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.token.Token;
import com.wrongweather.moipzy.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final TokenRepository tokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;

    //캘린더 업데이트 메서드
    public void getAllEvents() {
        log.info("getAllEvents");
        List<Token> tokens = tokenRepository.findAll();
        for (Token token : tokens) {
            int userId = token.getUserId();

            LocalDate today= LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            String todayEvents = getEvents(userId, today);
            String tomorrowEvents = getEvents(userId, tomorrow);

            redisTemplate.opsForHash().put(Integer.toString(userId), "today", todayEvents);
            redisTemplate.opsForHash().put(Integer.toString(userId), "tomorrow", tomorrowEvents);

            log.info("userId: {}, events: {}", userId, redisTemplate.opsForHash().get(Integer.toString(userId), "today"));
            log.info("userId: {}, events: {}", userId, redisTemplate.opsForHash().get(Integer.toString(userId), "tomorrow"));
        }
    }

    public String getEvents(int userId, LocalDate date) {

        try {

            if (!tokenRepository.existsByUserId(userId))
                return null;

            // Map to store events for the requested date with time
            Map<LocalDate, List<Map<String, String>>> eventMap = new HashMap<>();

            // Formatter for UTC time conversion
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            // Generate start and end times in UTC
            String startOfDay = date.atStartOfDay(ZoneId.of("Asia/Seoul")).withZoneSameInstant(ZoneId.of("UTC")).format(utcFormatter);
            String endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul")).withZoneSameInstant(ZoneId.of("UTC")).format(utcFormatter);

            // Get date's URL in UTC format
            String todayUrl = getUrl(startOfDay, endOfDay);

            // 구글 캘린더에 접근하기 위한 access token 얻기
            String accessToken = tokenRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException()).getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(todayUrl, HttpMethod.GET, request, String.class);
            String responseBody = response.getBody();

            List<Map<String, String>> events = getEventsWithTime(responseBody);

            eventMap.put(date, events);

            String result = "";

            // 각 날짜별 이벤트에서 summary만 추출
            for (Map.Entry<LocalDate, List<Map<String, String>>> entry : eventMap.entrySet()) {
                LocalDate date1 = entry.getKey();
                List<Map<String, String>> events1 = entry.getValue();

                // 각 이벤트에서 summary 추출
                StringBuilder eventDetails = new StringBuilder();
                for (Map<String, String> event : events1) {
                    String summary = event.get("summary");
                    String startTime = event.get("startTime");
                    String endTime = event.get("endTime");

                    if (summary != null && startTime != null && endTime != null) {
                        // startTime과 endTime에서 시각 추출
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                        LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
                        LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

                        String startFormatted = startDateTime.getHour() + "시 " + startDateTime.getMinute() + "분";
                        String endFormatted = endDateTime.getHour() + "시 " + endDateTime.getMinute() + "분";

                        // 이벤트 상세 정보 추가
                        eventDetails.append("- ").append(summary).append(" (")
                                .append(startFormatted).append("~")
                                .append(endFormatted).append(")")
                                .append("\n");
                    }
                }
                //날짜와 summary 출력
                if (eventDetails.length() > 0) {
                    // 마지막 쉼표 제거
                    eventDetails.setLength(eventDetails.length() - 2);
                    result += eventDetails.toString();
                    //result += "Date: " + date + " - " + eventDetails.toString() + "\n";
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

       return null;
    }

    @Async
    public CompletableFuture<String> getEventsTest(int userId, LocalDate date) {

        long start = System.currentTimeMillis();

        try {

            // Map to store events for the requested date with time
            Map<LocalDate, List<Map<String, String>>> eventMap = new HashMap<>();

            // Formatter for UTC time conversion
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            // Generate start and end times in UTC
            String startOfDay = date.atStartOfDay(ZoneOffset.UTC).format(utcFormatter);
            String endOfDay = date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).format(utcFormatter);

            // Get date's URL in UTC format
            String todayUrl = getUrl(startOfDay, endOfDay);

            // 구글 캘린더에 접근하기 위한 access token 얻기
            String accessToken = tokenRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException()).getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(todayUrl, HttpMethod.GET, request, String.class);

            String responseBody = response.getBody();
            List<Map<String, String>> events = getEventsWithTime(responseBody);

            log.info("events : {}", events);

            eventMap.put(date, events);

            String result = "Event: ";

            // 각 날짜별 이벤트에서 summary만 추출
            for (Map.Entry<LocalDate, List<Map<String, String>>> entry : eventMap.entrySet()) {
                LocalDate date1 = entry.getKey();
                List<Map<String, String>> events1 = entry.getValue();

                // 각 이벤트에서 summary 추출
                StringBuilder eventDetails = new StringBuilder();
                for (Map<String, String> event : events1) {
                    String summary = event.get("summary");
                    String startTime = event.get("startTime");
                    String endTime = event.get("endTime");

                    if (summary != null && startTime != null && endTime != null) {
                        // startTime과 endTime에서 시각 추출
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                        LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
                        LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

                        String startFormatted = startDateTime.getHour() + "시 " + startDateTime.getMinute() + "분";
                        String endFormatted = endDateTime.getHour() + "시 " + endDateTime.getMinute() + "분";

                        // 이벤트 상세 정보 추가
                        eventDetails.append("Summary: ").append(summary)
                                .append(", Start: ").append(startFormatted)
                                .append(", End: ").append(endFormatted)
                                .append("; ");
                    }
                }
                //날짜와 summary 출력
                if (eventDetails.length() > 0) {
                    // 마지막 쉼표 제거
                    eventDetails.setLength(eventDetails.length() - 2);
                    result += "Date: " + date + " - " + eventDetails.toString() + "\n";
                }
            }

            long end = System.currentTimeMillis();

            log.info("event time: {} ms", end - start);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
