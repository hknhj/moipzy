package com.wrongweather.moipzy.domain.crawling.exception;

import java.io.IOException;

public class CrawlingFailedException extends RuntimeException {
    public CrawlingFailedException(String message) {
        super(message);
    }

    public CrawlingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
