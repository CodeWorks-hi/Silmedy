package com.example.silmedy.ui.config;

import com.example.silmedy.model.LoginRequest;
import com.example.silmedy.model.LoginResponse;
import com.example.silmedy.model.RefreshTokenResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface ApiService {
    @POST("/patient/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/token/refresh")
    Call<RefreshTokenResponse> refreshToken();
}