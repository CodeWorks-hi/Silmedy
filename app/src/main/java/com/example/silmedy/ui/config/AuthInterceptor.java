package com.example.silmedy.ui.config;

import java.io.IOException;

import okhttp3.*;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 기존 Access Token 추가
        String accessToken = tokenManager.getAccessToken();
        Request.Builder builder = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken);

        Response response = chain.proceed(builder.build());

        // 401 Unauthorized → Refresh 토큰 시도
        if (response.code() == 401) {
            response.close(); // 기존 응답 닫기

            synchronized (this) {
                String newAccessToken = tokenManager.refreshAccessToken();
                if (newAccessToken != null) {
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            }
        }

        return response;
    }
}