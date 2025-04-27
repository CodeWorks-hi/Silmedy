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
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception e);
    }

    private static final String API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
    private static final String API_URL = BuildConfig.HUGGINGFACE_API_URL;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void sendChatStream(String userMessage, StreamCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        try {
            // 1) system prompt
            String systemPrompt =
                    "아래 형식에 맞춰 한국어로 답변해 주세요.\n\n" +
                            "질문: " + userMessage + "\n" +
                            "⸻\n" +
                            "안녕하세요. " + userMessage + "에 대해 안내해 드릴게요. ... (중략)";

            JSONObject systemObj = new JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt);

            // 2) user prompt
            JSONObject userObj = new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage);

            // 3) messages array
            JSONArray messages = new JSONArray()
                    .put(systemObj)
                    .put(userObj);

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

            // 5) SSE 파싱
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
