package com.wrongweather.moipzy.domain.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @InjectMocks
    private WeatherService weatherService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    RestTemplate restTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGetWeather() throws Exception {
        // given
        String mockResponseBody = """
                {
                    "list": [
                        {
                            "dt_txt": "2025-01-27 12:00:00",
                            "main": { "feels_like": -5.5 }
                        },
                        {
                            "dt_txt": "2025-01-27 13:00:00",
                            "main": { "feels_like": -4.1 }
                        },
                        {
                            "dt_txt": "2025-01-28 12:00:00",
                            "main": { "feels_like": -3.2 }
                        },
                        {
                            "dt_txt": "2025-01-28 13:00:00",
                            "main": { "feels_like": -2.2 }
                        }                        
                    ]
                }
                """;
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.openweathermap.org/data/2.5/forecast?lat=37&lon=127&units=metric&appid=EUiQWhksDIPHARk6M07HITUuY4R7T/RpUGo7tgwISoaAXZSLGyCgFctvFZ5FRXszbVzzfi0gDSDi66/g2ZcQ4g==")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockResponseBody));

        List<Integer> temp = weatherService.getWeather();

        System.out.println(temp);
    }

}
