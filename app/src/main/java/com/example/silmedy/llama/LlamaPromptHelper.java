package com.example.silmedy.llama;

import android.util.Log;

import com.example.silmedy.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LlamaPromptHelper {

    public interface StreamCallback {
        /** 새로운 텍스트 델타 청크가 도착할 때마다 호출됩니다. */
        void onChunk(String chunk);
        /** 스트리밍이 완전히 끝났을 때 호출됩니다. */
        void onComplete();
        /** 네트워크 오류나 파싱 에러 시 호출됩니다. */
        void onError(Exception e);
    }

    // build.gradle 에서 buildConfigField 로 주입된 값
    private static final String API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
    private static final String API_URL = BuildConfig.HUGGINGFACE_API_URL;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Hugging Face Router(또는 self-hosted) 스트리밍 엔드포인트로 ChatCompletionStream 호출
     *
     * @param userMessage        실제 사용자가 입력한 질문
     * @param callback           델타 청크/완료/에러를 받는 콜백
     */
    public static void sendChatStream(String userMessage, StreamCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        try {
            // 1) user 메시지
            JSONObject userObj = new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage);

            // 2) assistant 메시지 (예시)
            JSONObject assistantObj = new JSONObject()
                    .put("role", "assistant")
                    .put("content", "머리가아프고 배가 쓰려요\n");

            // 3) messages 배열 조립
            JSONArray messages = new JSONArray()
                    .put(userObj)
                    .put(assistantObj);

            // 4) request body
            JSONObject bodyJson = new JSONObject()
                    .put("provider", "fireworks-ai")
                    .put("model", "meta-llama/Llama-3.1-8B-Instruct")
                    .put("messages", messages)
                    .put("temperature", 0.7)
                    .put("max_tokens", 1024)
                    .put("top_p", 0.6)
                    .put("stream", true);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            // 5) 비동기 호출 및 SSE 파싱
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("HTTP " + response.code()));
                        return;
                    }
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String jsonPart = line.substring(6).trim();
                                if ("[DONE]".equals(jsonPart)) break;
                                JSONObject chunkObj = new JSONObject(jsonPart);
                                JSONArray choices = chunkObj.getJSONArray("choices");
                                JSONObject delta = choices
                                        .getJSONObject(0)
                                        .getJSONObject("delta");
                                if (delta.has("content")) {
                                    callback.onChunk(delta.getString("content"));
                                }
                            }
                        }
                        callback.onComplete();
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}
