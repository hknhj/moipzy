package com.wrongweather.moipzy.domain.chatGPT.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrongweather.moipzy.domain.chatGPT.dto.OutfitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public OutfitResponse getChatGPTResponse(String prompt) {

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // 요청 본문 설정
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", new Object[]{
                        Map.of("role", "system", "content", """
                                 You are an assistant that recommends clothing combinations based on the user's provided schedule and clothing list.\s
                                 Follow these detailed guidelines to recommend outfits in strict JSON format. Do not include any additional explanation, headers, or text.:
                                Output format:
                                ```json
                                {
                                  "outfits": [
                                    {
                                      "style": "(neat / semi-casual / comfortable)",
                                      "combination": {
                                        "outer": "(integer or null)",
                                        "top": "(integer)",
                                        "bottom": "(integer)"
                                      },
                                      "explanation": "(brief explanation)"
                                    }
                                  ]
                                }
                                 **General Rules for Clothing Combinations**:
                                 1. **Clothing Combination Restrictions**:
                                    - `outer` can only be outerwear items, `top` can only be tops, and `bottom` can only be bottoms.
                                    - D-SHIRT can be either `outer` or `top`, not both.
                                    - Include `outer` (optional), `top` (mandatory), and `bottom` (mandatory).
                                 2. **Explanation of outfit**:
                                      - A neat outfit (e.g., slacks or cotton pants for bottom. knit or dressed shirt as a top. Polo shirt alone, and blazers, blouson or coats as outerwear).
                                      - A semi-casual outfit (e.g., jeans or cotton pants with knit/hoodie/sweatshirt or jeans with long sleeve, T-shirt with cardigan, denim jacket, or MA1 jacket. Hoodie with a coat, and MA1 might be also good).
                                      - A comfortable outfit (e.g., sweatpants with a hoodie or sweatshirt).
                                      - Do not recommend knit/hoodie/sweatshirt with cardigan, denim_jacket, blouson, blazer, hooded jackets.
                                      - Whenever possible, recommend clothes that haven't been worn recently.
                                      - Recommend outer when lowTemp is under 12°C
                                 3. **Schedule Rules**:
                                    - Prioritize events with higher formality or importance.
                                    - For formal events (e.g., meetings, weddings, funerals):
                                      - Recommend neat outfits including blazers, coats, slacks, or cotton pants with knit or polo shirts.
                                    - For casual events (e.g., dinner with friends, casual outings):
                                      - Include jeans in recommendations for versatility.
                                    - For dates:
                                      - Provide three options: one formal (neat), one semi-casual (jeans + knit/hoodie/sweatshirt), and one relaxed (comfortable wear).
                                    - No schedule provided:
                                      - Recommend neat, semi-casual, comfortable each.
                                      
                                 Follow these guidelines closely when making recommendations.
                """),
                        Map.of("role", "user", "content", prompt+ "Based on the available clothing options, recommend upto three outfits. Provide the output strictly in the following JSON structure without any additional text:")
                },
                "max_tokens", 1500
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // OpenAI API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null) {
            // 'choices' 배열에서 첫 번째 요소 추출
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                if (message != null) {
                    String messageText = (String) message.get("content");

                    // '```json' 및 '```' 제거
                    if (messageText.startsWith("```json")) {
                        messageText = messageText.substring(7).trim(); // '```json' 제거
                    }
                    if (messageText.endsWith("```")) {
                        messageText = messageText.substring(0, messageText.length() - 3).trim(); // '```' 제거
                    }

                    try{
                        return objectMapper.readValue(messageText, OutfitResponse.class);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        }
        return null;
    }


}
