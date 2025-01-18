package com.example.wildtide;
import java.util.ArrayList;

public class Lockey_CredResponse {
    private boolean success;
    private ArrayList<Lockey_Credentials> credentials;
    private String message;
    public Lockey_CredResponse(boolean success, ArrayList<Lockey_Credentials> credentials, String message) {
        this.credentials = credentials;
        this.success = success;
        this.message = message;
    }
    public ArrayList<Lockey_Credentials> getCredentials() {
        return credentials;
    }
    public boolean getSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
}