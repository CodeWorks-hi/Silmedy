package com.example.silmedy.ui.config;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // Access Token 설정
        String accessToken = tokenManager.getAccessToken();  // SharedPreferences 등에서 가져옴
        Request.Builder builder = request.newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken);

        Response response = chain.proceed(builder.build());

        // 토큰 만료 시
        if (response.code() == 401) {
            synchronized (this) {
                // Refresh Token 요청
                String newAccessToken = tokenManager.refreshAccessToken();
                if (newAccessToken != null) {
                    // 재요청
                    Request newRequest = request.newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            }
        }

        return response;
    }
}