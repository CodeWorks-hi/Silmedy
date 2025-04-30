package com.example.silmedy.llama;

import android.util.Log;
import com.example.silmedy.BuildConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * LlamaClassifier:
 * 1) 문장 기반 진료과 분류 (외과 키워드 감지 + Llama 3.1 분류)
 * 2) 증상 기반 AI 문진 스트리밍 헬퍼
 */
public class LlamaClassifier {
    private static final String TAG = "LlamaClassifier";
    private static final String API_URL = BuildConfig.HUGGINGFACE_API_URL + "/chat/completions";
    private static final String API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 외과 키워드 목록
    private static final List<String> SURGICAL_KEYWORDS = Arrays.asList(
            "외과", "수술", "절개", "골절", "탈구", "출혈", "상처", "깁스"
    );

    /**
     * 분류 콜백
     */
    public interface ClassificationCallback {
        void onSurgicalQuestion(String prompt);

        void onClassification(String category);

        void onError(Exception e);
    }

    /**
     * 1) 외과 키워드 감지 → onSurgicalQuestion
     * 2) 일반 텍스트 분류 → onClassification
     */
    public void classifyOrPrompt(String sentence, ClassificationCallback cb) {
        String lower = sentence.toLowerCase(Locale.ROOT);
        for (String kw : SURGICAL_KEYWORDS) {
            if (lower.contains(kw)) {
                cb.onSurgicalQuestion(
                        "외과 진료가 필요해 보여요.\n" +
                                "편하실 때 촬영을 통해 증상을 확인해 보실 수 있습니다.\n" +
                                "지금 터치로 증상 확인 페이지로 이동해 보시겠어요? (예/아니오)"
                );
                return;
            }
        }

        try {
            JSONObject system = new JSONObject()
                    .put("role", "system")
                    .put("content",
                            "너는 내과 관련 AI 상담 전문가야. " +
                                    "아래 환자의 질문에 환자가 불안해하지 않도록 친절하게 안내하고, " +
                                    "아래 문장을 ‘외과’ 또는 ‘내과’ 중 하나로 분류해. " +
                                    "반드시 한국어로만 답해.");
            JSONObject user = new JSONObject()
                    .put("role", "user")
                    .put("content", "문장: \"" + sentence + "\"");
            JSONArray messages = new JSONArray().put(system).put(user);
            JSONObject body = new JSONObject()
                    .put("model", "meta-llama/Llama-3.1-8B-Instruct")
                    .put("messages", messages)
                    .put("max_tokens", 5)
                    .put("temperature", 0.0)
                    .put("stream", false);

            Request req = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            new OkHttpClient().newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    cb.onError(e);
                }

                @Override
                public void onResponse(Call call, Response resp) throws IOException {
                    if (!resp.isSuccessful()) {
                        cb.onError(new IOException("HTTP " + resp.code()));
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(resp.body().string());
                        String cat = res.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();
                        cb.onClassification(cat);
                    } catch (Exception e) {
                        cb.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            cb.onError(e);
        }
    }

    /**
     * AI 문진·케어 스트리밍 헬퍼 (static inner class)
     */
    public static class LlamaPromptHelper {
        private static final String HELPER_TAG = "LlamaPromptHelper";
        private static final MediaType HELPER_JSON = MediaType.get("application/json; charset=utf-8");
        private static final String HELPER_API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
        private static final String HELPER_API_URL = BuildConfig.HUGGINGFACE_API_URL;

        public interface StreamCallback {
            void onChunk(String chunk);

            void onComplete();

            void onError(Exception e);
        }

        public static void sendChatStream(
                String userId,
                String prevSymptom,
                String userMessage,
                StreamCallback callback
        ) {
            Log.d(HELPER_TAG, "API_URL=" + HELPER_API_URL);
            if (HELPER_API_URL == null || !HELPER_API_URL.startsWith("http")) {
                callback.onError(new IllegalArgumentException("Invalid API_URL: " + HELPER_API_URL));
                return;
            }

            boolean isCombined = prevSymptom != null && !prevSymptom.isEmpty();
            String prompt = buildSystemPrompt(prevSymptom, userMessage, isCombined);

            try {
                JSONObject systemObj = new JSONObject()
                        .put("role", "system")
                        .put("content", prompt);
                JSONObject userObj = new JSONObject()
                        .put("role", "user")
                        .put("content", userMessage);
                JSONArray messages = new JSONArray().put(systemObj).put(userObj);
                JSONObject bodyJson = new JSONObject()
                        .put("model", "meta-llama/Llama-3.1-8B-Instruct")
                        .put("messages", messages)
                        .put("max_tokens", 512)
                        .put("temperature", 0.3)
                        .put("top_p", 0.8)
                        .put("stream", true);

                OkHttpClient client = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .build();
                Request request = new Request.Builder()
                        .url(HELPER_API_URL + "/chat/completions")
                        .addHeader("Authorization", "Bearer " + HELPER_API_KEY)
                        .post(RequestBody.create(bodyJson.toString(), HELPER_JSON))
                        .build();

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

        // 시스템 프롬프트 빌드
        private static String buildSystemPrompt(String prevSymptom, String userMessage, boolean isCombined) {
            StringBuilder sb = new StringBuilder()
                    .append("안녕하세요!🩺 저는 Slimedy AI 닥터링(Dr.Link)입니다.\n")
                    .append("궁금한 증상을 말씀해 주시면 쉽게 안내해 드릴게요.\n\n");

            if (isCombined) {
                sb.append("✏️ [복합 증상 분석]\n")
                        .append("- 증상1: ").append(prevSymptom).append("\n")
                        .append("- 증상2: ").append(userMessage).append("\n")
                        .append("- 원인 예시:\n  1) ")
                        .append(getCombinedCause(prevSymptom, userMessage))
                        .append("\n  2) 스트레스 또는 일시적 피로\n\n");
            } else {
                sb.append("✏️ [증상 분석]\n")
                        .append("- 증상: ").append(userMessage).append("\n")
                        .append("- 원인 예시:\n")
                        .append(getSingleCauseExamples(userMessage))
                        .append("\n\n");
            }

            sb.append("🏠 [집에서 할 수 있는 관리]\n")
                    .append(getGeneralCareAdvice())
                    .append(getSymptomSpecificAdvice(userMessage))
                    .append("\n⚠️ [이럴 땐 병원 방문]\n")
                    .append(getEmergencyIndicators(userMessage))
                    .append("\n\n비대면 진료가 필요하시면 '예'라고 답해주세요.");

            return sb.toString();
        }

        private static String getSingleCauseExamples(String symptom) {
            if (symptom.contains("두통")) {
                return "  1) 근육 긴장\n  2) 탈수\n  3) 눈 피로";
            } else if (symptom.contains("복통")) {
                return "  1) 소화불량\n  2) 가스\n  3) 과식";
            } else if (symptom.contains("열")) {
                return "  1) 감염\n  2) 염증\n  3) 과로";
            }
            return "  1) 일시적 피로\n  2) 환경 변화";
        }

        private static String getCombinedCause(String s1, String s2) {
            if (s1.contains("두통") && s2.contains("메슥거림")) {
                return "위장 관련 두통 (과식/스트레스)";
            }
            if (s1.contains("열") && s2.contains("기침")) {
                return "감기/독감";
            }
            return "일시적 신체 반응";
        }

        private static String getGeneralCareAdvice() {
            return "  → 30분마다 미지근한 물을 조금씩 마셔보세요\n"
                    + "    (단, 신장질환·심부전·부종·삼킴 곤란·금식 등은 의료진과 상의)\n"
                    + "  → 1~2시간 편하게 쉬세요\n"
                    + "  → 증상과 체온을 기록해 두세요\n";
        }

        private static String getSymptomSpecificAdvice(String symptom) {
            StringBuilder sb = new StringBuilder();
            if (symptom.contains("열")) {
                sb.append("  → 38.5℃ 이상이면 해열제 복용\n");
            }
            if (symptom.contains("통증")) {
                sb.append("  → 아픈 부위 5분간 찜질\n");
            }
            if (symptom.matches(".*(구토|설사).*")) {
                sb.append("  → 이온음료(전해질 음료)로 수분 보충\n");
            }
            return sb.toString();
        }

        private static String getEmergencyIndicators(String symptom) {
            List<String> signs = new ArrayList<>();
            signs.add("증상이 6시간 이상 계속됨");
            if (symptom.matches(".*(흉통|호흡곤란).*")) {
                signs.add("가슴 답답/숨쉬기 힘듦 → 119 신고");
            }
            if (symptom.contains("의식저하")) {
                signs.add("의식이 흐려짐/이상 행동");
            }
            return "• " + String.join("\n• ", signs);
        }
    }
}
