package com.example.SWP391_SPRING2026.Exception;

public class DuplicateResourceException extends RuntimeException {
    private final String code;

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
