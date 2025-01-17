package com.wrongweather.moipzy.domain.users.exception;

public class LoginFailedException extends RuntimeException{
    public LoginFailedException() {
        super("Wrong id or password");
    }
}
