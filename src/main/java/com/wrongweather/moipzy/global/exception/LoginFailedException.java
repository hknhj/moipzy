package com.wrongweather.moipzy.global.exception;

import lombok.NoArgsConstructor;

public class LoginFailedException extends RuntimeException{

    public LoginFailedException() {
        super("Wrong id or password");
    }
}
