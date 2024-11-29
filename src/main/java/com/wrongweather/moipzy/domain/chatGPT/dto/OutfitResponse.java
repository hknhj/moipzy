package com.wrongweather.moipzy.domain.chatGPT.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor //objectMapper를 사용할 때에는 기본 생성자가 필요하다
public class OutfitResponse {
    private List<Response> outfits;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String style;
        private Combination combination;
        private String explanation;

        public Response(String style, Combination combination, String explanation) {
            this.style = style;
            this.combination = combination;
            this.explanation = explanation;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Combination {
        private int outer;
        private int top;
        private int bottom;

        public Combination(String outer, String top, String bottom) {
            this.outer = Integer.parseInt(outer);
            this.top = Integer.parseInt(top);
            this.bottom = Integer.parseInt(bottom);
        }
    }
}
