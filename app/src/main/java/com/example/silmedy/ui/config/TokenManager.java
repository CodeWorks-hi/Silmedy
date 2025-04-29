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
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("SilmedyPrefs", Context.MODE_PRIVATE);
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

    public interface TokenRefreshCallback {
        void onTokenRefreshed(String newAccessToken);
    }

    public void refreshAccessTokenAsync(TokenRefreshCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://43.201.73.161:5000/token/refresh")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + getRefreshToken())
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    try {
                        JSONObject json = new JSONObject(body);
                        String newAccessToken = json.getString("access_token");
                        saveAccessToken(newAccessToken);
                        if (callback != null) {
                            callback.onTokenRefreshed(newAccessToken);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}