package com.example.wildtide.lockey;

public class BoolResponse {
    private boolean success;
    private String message;
    public BoolResponse(boolean success, String message) {
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