package com.wrongweather.moipzy.domain.chatGPT.service;

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

    public String getChatGPTResponse(String prompt) {
        System.out.println(prompt);

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // 요청 본문 설정
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", new Object[]{
                        Map.of("role", "system", "content", """
                        You are an assistant that recommends clothing combinations based on the user's provided schedule and clothing list. Follow these detailed guidelines to recommend outfits in strict JSON format. Do not include any additional explanation, headers, or text. Only provide output in this exact JSON structure:
                          
                                {
                                  "outfits": [
                                    {
                                      "style": "choose one from (neat / semi-casual / comfortable)",
                                      "combination": {
                                        "outer": "[outer's cloth id in provided list of cloth]",
                                        "top": "[top's cloth id in provided list of cloth]",
                                        "bottom": "[bottom's cloth id in provided list of cloth]"
                                      },
                                      "explanation": "[explanation text]"
                                    },
                                    {
                                      "style": "choose one from (neat / semi-casual / comfortable)",
                                      "combination": {
                                        "outer": "[outer's cloth id in provided list of cloth]",
                                        "top": "[top's cloth id in provided list of cloth]",
                                        "bottom": "[bottom's cloth id in provided list of cloth]"
                                      },
                                      "explanation": "[explanation text]"
                                    },
                                    {
                                      "style": "choose one from (neat / semi-casual / comfortable)",
                                      "combination": {
                                        "outer": "[outer's cloth id in provided list of cloth]",
                                        "top": "[top's cloth id in provided list of cloth]",
                                        "bottom": "[bottom's cloth id in provided list of cloth]"
                                      },
                                      "explanation": "[explanation text]"
                                    }
                                  ]
                                }
                        
                        1. **General Rules for Clothing Tones and Combinations**:
                           - Colors are divided into dark tones (black, charcoal, navy, brown, olive) and bright tones (light grey, cream, beige).
                           - Outfit combinations include:
                             - Dark-Dark-Dark (Outer-Bottom-Top): e.g., (black, black, charcoal), (charcoal, charcoal, olive)
                             - Dark-Dark-Bright: e.g., (black, black, light grey), (navy, brown, beige)
                             - Dark-Bright-Dark: e.g., (black, light grey, black), (brown, beige, brown)
                             - Dark-Bright-Bright: e.g., (black, light grey, light grey), (navy, beige, cream)
                             - Bright-Bright-Bright: e.g., (beige, cream, cream), (light grey, beige, cream)
                             - Bright-Dark-Dark: e.g., (beige, charcoal, charcoal), (cream, black, black)
                             - Bright-Bright-Dark: e.g., (light grey, light grey, black), (beige, cream, charcoal)
                             - Bright-Dark-Bright: e.g., (beige, black, cream), (light grey, navy, light grey)
                             - For combinations with two bright items, ensure a tonal contrast between the bright colors.
                           
                        2. **When no schedule is provided**:
                           - Suggest three outfits:
                             - A neat outfit (e.g., slacks or cotton pants for bottom. knit or dressed shirt as a top. Polo shirt alone, and blazers, blouson or coats as outerwear).
                             - A semi-casual outfit (e.g., jeans or cotton pants with knit/hoodie/sweatshirt or jeans with long sleeve, T-shirt with cardigan, denim jacket, or MA1 jacket. Hoodie with a coat, and MA1 might be also good).
                             - A comfortable outfit (e.g., sweatpants with a hoodie or sweatshirt).
                        
                        3. **When a schedule is provided**:
                           - Prioritize events with higher formality or importance.
                           - For formal events (e.g., meetings, weddings, funerals):
                             - Recommend neat outfits like blazers, coats, slacks, or cotton pants with knit or polo shirts.
                           - For casual events (e.g., dinner with friends, casual outings):
                             - Include jeans in recommendations for versatility.
                           - For dates:
                             - Provide three options: one formal (neat), one semi-casual (jeans + knit/hoodie/sweatshirt), and one relaxed (comfortable wear).
                           - For formal settings, avoid casual styles like sweatpants or hoodies.
                        
                        4. **Additional Style-Specific Rules**:
                           - Jeans are versatile and suitable for most casual events.
                           - When suggesting a semi-casual style, avoid recommending sweaters as tops if knitwear or sweatshirts are used as outerwear.
                           - For formal looks, prioritize clean designs like slacks, cotton pants, and blazers.
                           - For semi-casual outfits, outerwear like cardigans, denim jackets, stadium jackets, MA1, or leather jackets is preferred.
                           - Do not recommend knit/hoodie/sweatshirt with cardigan, denim_jacket, blouson, blazer, hooded
                           - D-SHIRT can be worn as either outer or top.
                        
                        5. **Responding to user input**:
                           - When the user provides a schedule and a list of clothes, prioritize the schedule and recommend outfits accordingly.
                           - If no schedule is provided, recommend based on neat, semi-casual, and relaxed styles.
                           - Whenever possible, recommend clothes that haven't been worn recently.
                        
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

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            // 'choices' 배열에서 첫 번째 요소 추출
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        }
        return "No response from ChatGPT.";
    }
}
