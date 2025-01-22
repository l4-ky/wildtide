package com.example.wildtide.lockey;
import java.util.ArrayList;

public class CredResponse {
    private boolean success;
    private ArrayList<Credentials> credentials;
    private String message;
    public CredResponse(boolean success, ArrayList<Credentials> credentials, String message) {
        this.credentials = credentials;
        this.success = success;
        this.message = message;
    }
    public ArrayList<Credentials> getCredentials() {
        return credentials;
    }
    public boolean getSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
}