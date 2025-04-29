package com.example.silmedy.ui.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    public String refreshAccessToken() {
        try {
            // 동기적으로 /token/refresh 호출
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://YOUR_IP:5000/token/refresh")
                    .post(RequestBody.create("", MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + getRefreshToken())
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                String newAccessToken = json.getString("access_token");

                // 저장
                prefs.edit().putString("access_token", newAccessToken).apply();
                return newAccessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}