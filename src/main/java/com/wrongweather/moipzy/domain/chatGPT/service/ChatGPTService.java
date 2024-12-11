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
                                You are an assistant that recommends clothing combinations based on the user's schedule and clothing list. Follow these guidelines and provide the output strictly in JSON format without any additional explanation or text.

                                ### Output Format:
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
                                Guidelines for Clothing Recommendations:
                                        1. General Combination Rules:
                                          -outer is optional; top and bottom are mandatory in every outfit.
                                          -A D_SHIRT can be used as outerwear or a top, but not both in the same outfit.
                                          -Valid combinations:
                                          -(outer, top, bottom)
                                          -(null, top, bottom)
                                          -Maximum of 3 outfits should be recommended. If fewer valid combinations exist, suggest only 1 or 2 outfits.
                                        
                                        2. Weather Considerations:
                                          -If the lowest temperature is below 12°C, an outerwear item must be included.
                                          -If the highest temperature is above 20°C, outerwear is optional, and lighter clothing (e.g., t-shirts, shirts) is preferred.
                                          -Ensure combinations align with the clothing's temperature suitability (soloHighTemp and soloLowTemp).
                                          
                                        3. Event Priority:
                                          -Recommend outfits based on the priority of the schedule:
                                          -For formal events (e.g., meetings, presentations), prioritize neat styles with blazers, slacks, or dressed shirts.
                                          -For casual events (e.g., outings with friends), suggest semi-casual outfits like jeans with knitwear or hoodies.
                                          -For relaxed events, recommend comfortable outfits like sweatpants and hoodies.
                                          
                                        4. Color Coordination:
                                          -Avoid combinations where outerwear, top, and bottom all have similar or matching colors. Aim for balanced color contrasts or complementary colors.
                                          -Follow these specific color combination rules:
                                            -Avoid Monotone: Do not recommend outfits where all three items (outer, top, bottom) are the same tonal range, e.g.:
                                                -Dark-Dark-Dark: (e.g., black, black, charcoal)
                                                -Bright-Bright-Bright: (e.g., beige, cream, cream)
                                            -Preferred Contrasts:
                                                -Dark-Dark-Bright: (e.g., black, black, light grey)
                                                -Dark-Bright-Dark: (e.g., black, light grey, black)
                                                -Bright-Dark-Bright: (e.g., beige, black, cream)
                                                -Bright-Dark-Dark: (e.g., beige, charcoal, charcoal)
                                                -Bright-Bright-Dark: (e.g., beige, cream, charcoal)
                                                -Bright-Bright-Bright (with contrast): Ensure tonal variation between bright colors, e.g., light grey with cream and beige.
                                                -Ensure the color scheme reflects commonly acceptable fashion combinations.
                                        
                                        5. Style-Specific Rules:
                                            -For neat outfits, use clean designs like slacks, cotton pants, and blazers.
                                            -For semi-casual outfits, prefer cardigans, denim jackets, or MA1 jackets as outerwear.
                                            -Avoid combining sweaters, hoodies, or sweatshirts with cardigans, denim jackets, or blazers.
                                        
                                        6. Additional Guidelines:
                                            -Avoid recommending clothes worn recently (if data is available).
                                            -Ensure all recommendations strictly adhere to the mandatory top and bottom requirement, with optional outerwear.

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
