package com.wrongweather.moipzy.domain.token.dto;

import lombok.Data;

@Data
public class GoogleTokenResponse {
    private String access_token;
    private String token_type;
    private String scope;
    private String id_token;
    private int expires_in;
}
