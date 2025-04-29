package com.example.silmedy.llama;

import android.util.Log;
import com.example.silmedy.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    // 외과 키워드 목록
    private static final List<String> SURGICAL_KEYWORDS = Arrays.asList(
            "외과", "수술", "절개", "봉합", "골절", "탈구", "출혈", "상처", "깁스"
    );

    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception e);
    }

    /**
     * 외과 관련 질문 감지
     */
    private static boolean isSurgicalCase(String message) {
        if (message == null) return false;
        String lowerMsg = message.toLowerCase(Locale.ROOT);
        return SURGICAL_KEYWORDS.stream().anyMatch(lowerMsg::contains);
    }

    public static void sendChatStream(
            String userId,
            String prevSymptom,
            String userMessage,
            StreamCallback callback
    ) {

        Log.d(TAG, "HF_API_KEY = '" + API_KEY + "'");
        Log.d(TAG, "HF_API_URL = '" + API_URL + "'");

        String rawBase = API_URL;
        if (rawBase == null || rawBase.isBlank() ||
                !(rawBase.startsWith("http://") || rawBase.startsWith("https://"))) {
            callback.onError(new IllegalArgumentException("Invalid HF_API_URL: " + rawBase));
            return;
        }
        String endpoint = rawBase + "/chat/completions";

        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        try {
            // 2. 증상 유형 판별
            boolean isCombined = prevSymptom != null && !prevSymptom.isEmpty();

            // 3. 동적 시스템 프롬프트 생성
            String systemPrompt = buildSystemPrompt(prevSymptom, userMessage, isCombined);

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

    private static String buildSystemPrompt(String prevSymptom,
                                            String userMessage,
                                            boolean isCombined) {
        StringBuilder prompt = new StringBuilder()
                .append("안녕하세요! 여러분의 증상을 듣고 도움을 드릴 Slimedy AI 입니다 🩺\n\n");

        if (isCombined) {
            prompt.append("✏️ [복합 증상 분석]\n")
                    .append("• 첫 번째 증상: ").append(prevSymptom).append("\n")
                    .append("• 두 번째 증상: ").append(userMessage).append("\n")
                    .append("• 가능한 연관성:\n")
                    .append("  1) ").append(prevSymptom).append(" → ").append(userMessage).append(" (원인 추정)\n")
                    .append("  2) 별개 증상 동시 발생 가능성\n\n");
        } else {
            prompt.append("✏️ [단일 증상 분석]\n")
                    .append("• 주요 증상: ").append(userMessage).append("\n\n");
        }

        prompt.append("🏠 집에서 시도해 볼 응급 조치\n")
                .append("• ").append(userMessage).append(" 증상 완화법\n")
                .append("  → 1시간 누워 휴식 + 미지근한 물 마시기 (예시)\n\n")
                .append("⏰ 병원 방문이 필요한 경우\n")
                .append("• 통증 강도가 7/10 이상\n")
                .append("• 39℃ 이상 고열 12시간 지속\n\n")
                .append("비대면 진료로 편하게 상담받아보시겠어요?\n");

        return prompt.toString();
    }
}
