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
    private static final String TAG = "LlamaPromptHelper";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final String API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
    public static final String API_URL = BuildConfig.HUGGINGFACE_API_URL;

    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception e);
    }

    public static void sendChatStream(
            String prevSymptom,
            String userMessage,
            StreamCallback callback
    ) {
        Log.d(TAG, "HF_API_KEY  = '" + API_KEY + "'");
        Log.d(TAG, "HF_API_URL  = '" + API_URL + "'");

        String rawBase = API_URL;
        if (rawBase == null || rawBase.isBlank() ||
                !(rawBase.startsWith("http://") || rawBase.startsWith("https://"))) {
            callback.onError(new IllegalArgumentException("Invalid HF_API_URL: " + rawBase));
            return;
        }

        final String endpoint = rawBase + "/chat/completions";

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        try {
            String systemPrompt =
                    "사용자가 입력한 증상에 대해,\n" +
                            "1. 가능한 원인을 2~3개 정도 간단히 설명하고,\n" +
                            "2. 집에서 할 수 있는 관리 방법을 2~3개 제시하고,\n" +
                            "3. 병원에 가야 하는 경우를 2개 이상 안내하세요.\n\n" +
                            "설명은 친절하고 불안하지 않게, 짧은 문장으로 작성합니다.\n" +
                            "항목별로 ✔️, • 등을 사용해 시각적으로 구분합니다.\n" +
                            "모든 대답 마지막에는 '비대면 의사 진료를 진행하시겠습니까?' 문구를 추가하세요. 🌷\n\n" +
                            "첫 번째 증상: " + prevSymptom + "\n" +
                            "두 번째 증상: " + userMessage + "\n\n" +
                            "예시)\n" +
                            "첫 번째 증상: 배가 바늘로 찌르는 듯 아파요.\n" +
                            "두 번째 증상: 속이 더부룩하고 답답해요.\n\n" +
                            "✔️ 가능한 원인\n" +
                            "• 위산 과다\n" +
                            "• 급성 위염\n\n" +
                            "✔️ 집에서 관리하는 방법\n" +
                            "1. 따뜻한 물을 천천히 마시기\n" +
                            "2. 식후 바로 눕지 않기\n\n" +
                            "✔️ 병원에 가야 하는 경우\n" +
                            "• 통증이 심하거나 1시간 이상 지속될 때\n" +
                            "• 구토나 혈변이 있을 때\n\n" +
                            "마지막에 '비대면 의사 진료를 진행하시겠습니까?' 라는 문구로 마무리하세요.";

            JSONObject systemObj = new JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt);
            JSONObject userObj = new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage);
            JSONArray messages = new JSONArray()
                    .put(systemObj)
                    .put(userObj);

            JSONObject bodyJson = new JSONObject()
                    .put("model", "meta-llama/Llama-3.1-8B-Instruct")
                    .put("messages", messages)
                    .put("max_tokens", 512)
                    .put("temperature", 0.3)
                    .put("top_p", 0.8)
                    .put("stream", true);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    callback.onError(e);
                }
                @Override public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("HTTP " + response.code()));
                        return;
                    }
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data: ")) continue;
                            String jsonPart = line.substring(6).trim();
                            if ("[DONE]".equals(jsonPart)) break;
                            JSONObject chunkObj = new JSONObject(jsonPart);
                            String content = chunkObj
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("delta")
                                    .optString("content");
                            if (!content.isEmpty()) {
                                callback.onChunk(content);
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