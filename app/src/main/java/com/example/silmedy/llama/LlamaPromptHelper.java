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
    private static final String TAG     = "LlamaPromptHelper";
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

        // 1) baseURL 검증
        String rawBase = API_URL;
        if (rawBase == null || rawBase.isBlank() ||
                !(rawBase.startsWith("http://") || rawBase.startsWith("https://"))) {
            callback.onError(new IllegalArgumentException("Invalid HF_API_URL: " + rawBase));
            return;
        }
        // 2) 엔드포인트 조합
        final String endpoint = rawBase + "/chat/completions";

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        try {
            String systemPrompt =
                    "안녕하세요. 많이 힘드셨죠? 걱정하지 마세요. 😊\n\n" +
                            "첫 번째 증상: " + prevSymptom + "\n" +
                            "새로운 증상: "   + userMessage  + "\n" +
                            "⸻\n" +
                            "콧물과 가래, 두통은 서로 연관될 수 있어요. 몸이 병균과 싸우는 자연스러운 과정입니다.\n\n" +
                            "왜 이런 증상이 생길까요?\n" +
                            "   • 부비동염 → 코막힘·콧물·가래와 두통이 함께 나타날 수 있어요.\n" +
                            "   • 근육 긴장성 두통 → 목·어깨 긴장으로 발생하기도 해요.\n\n" +
                            "병원에 가야 할 때\n" +
                            "   • 38℃ 이상의 고열이 있을 때\n" +
                            "   • 숨쉬기 어렵거나 통증이 심할 때\n" +
                            "   • 콧물·가래에 피가 섞이거나 1주일 이상 증상이 지속될 때\n\n" +
                            "집에서 쉽게 해볼 수 있는 방법\n" +
                            "   • 따뜻한 소금물로 코 세척하기\n" +
                            "   • 따뜻한 물 자주 마시기\n" +
                            "   • 목·어깨 스트레칭으로 긴장 풀기\n" +
                            "   • 충분한 휴식과 수분 섭취 유지하기\n\n" +
                            "대부분은 시간이 지나면 좋아져요. 🌷\n" +
                            "혹시 가래 색깔이나 다른 증상이 있으면 알려주세요. 더 꼼꼼히 도와드릴게요! 🌼";

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
