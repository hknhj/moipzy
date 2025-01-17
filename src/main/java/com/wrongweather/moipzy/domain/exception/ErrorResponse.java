package com.wrongweather.moipzy.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;

    @Override
    public String toString() {
        return "ErrorResponse [code=" + code + ", message=" + message + "]";
    }
}
