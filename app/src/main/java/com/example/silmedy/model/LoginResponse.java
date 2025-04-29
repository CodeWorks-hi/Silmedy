package com.example.silmedy.model;

public class LoginResponse {
    private String access_token;
    private String refresh_token;
    private String username;

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public String getUserName() {
        return username;
    }
}