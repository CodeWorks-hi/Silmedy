package com.example.silmedy.ui.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class TokenManager {
    private final SharedPreferences prefs;
    private final Context context;

    public TokenManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public String getAccessToken() {
        return prefs.getString("access_token", null);
    }

    public String getRefreshToken() {
        return prefs.getString("refresh_token", null);
    }

    public void saveAccessToken(String token) {
        prefs.edit().putString("access_token", token).apply();
    }

    public String refreshAccessToken() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.0.170:5000/token/refresh")
                    .post(RequestBody.create("", MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + getRefreshToken())
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                String newAccessToken = json.getString("access_token");
                saveAccessToken(newAccessToken);
                return newAccessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}