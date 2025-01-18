package com.example.wildtide;

public class Lockey_BoolResponse {
    private boolean success;
    private String message;
    public Lockey_BoolResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
}