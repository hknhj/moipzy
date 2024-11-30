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
                                You are an assistant that recommends clothing combinations based on the user's provided schedule and clothing list. Follow these guidelines to recommend outfits in strict JSON format. Do not include any additional explanation, headers, or text.
                                         
                                1. Recommend up to 3 valid outfits based on inputs: highTemp, lowTemp, and optional event.
                                         
                                2. Valid combinations:
                                   - (outer, top, bottom) or (null, top, bottom).
                                   - Outer is optional, but top and bottom are mandatory.
                                   - D_SHIRT can be either outer or top, but not both.
                                         
                                3. Recommendation priority:
                                   - Prioritize clothing items that have not been worn recently.
                                         
                                4. Temperature logic:
                                   - If highTemp fits within a top's soloHighTemperature and soloLowTemperature range, recommend that top without an outer.
                                         
                                5. Event-based preferences:
                                   - For formal events (wedding, meeting, funeral), recommend **neat style**.
                                   - **Neat style:** blazer/coat/blouson as outer, polo/dressed shirt/knit as top, slacks/cotton pants as bottom.
                                   - **Semi-casual style:** jeans/cotton pants with knit/hoodie/sweatshirt or long sleeve T-shirt with cardigan/denim jacket/MA1 jacket.
                                   - **Comfortable style:** cotton pants/linen pants/shorts as bottom, sweatpants with hoodie or sweatshirt.
                                         
                                6. Style exclusions:
                                   - Do not pair knit/hoodie/sweatshirt with cardigan, denim jacket, blouson, blazer, or hooded jackets.
                                         
                                7. Output format:
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
