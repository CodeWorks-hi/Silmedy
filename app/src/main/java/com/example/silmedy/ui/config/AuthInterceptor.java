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

        // 401 Unauthorized 발생 시 별도 처리 없이 반환
        if (response.code() == 401) {
            response.close();
            // 401 처리: 앱 레벨에서 재로그인 유도
        }

        return response;
    }
}