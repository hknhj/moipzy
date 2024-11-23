package com.wrongweather.moipzy.domain.chatGPT.controller;

import com.wrongweather.moipzy.domain.chatGPT.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    @PostMapping
    public String chat(@RequestBody String prompt) {
        return chatGPTService.getChatGPTResponse(prompt);
    }
}
