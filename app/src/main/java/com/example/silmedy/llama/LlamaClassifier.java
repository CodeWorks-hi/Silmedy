package com.example.silmedy.llama;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LlamaClassifier {

    public interface Callback {
        void onResult(Message message);
        void onError(Exception e);
    }

    public interface SeparatorCallback {
        void onResult(ArrayList<String> parts, ArrayList<String> symptoms);
    }

    private static final String SAVE_URL = "http://43.201.73.161:5000/chat/save";
    private static final String SEP_URL  = "http://43.201.73.161:5000/chat/add-separator";

    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler;

    public LlamaClassifier(Context context) {
        this.context     = context.getApplicationContext();
        this.client      = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 1) Quick local check for 외과 / 기타 keywords
     * 2) If neither, POST to server + parse labels
     */
    public void classifySymptom(String patientText, String ptTs, Callback cb) {
        String norm = patientText.toLowerCase();

        // 1-a) 외과 긴급 키워드
        String[] surgical = {"골절", "뼈 부러짐", "상처", "출혈", "멍"};
        for (String kw : surgical) {
            if (norm.contains(kw)) {
                Message aiMsg = new Message(
                        "AI",
                        "외과 진료가 필요해 보여요.\n" +
                                "편하실 때 촬영을 통해 증상을 확인해 보실 수 있습니다.\n" +
                                "지금 터치로 증상 확인 페이지로 이동해 보시겠어요? (예/아니오)",
                        Message.formatTimeOnly(System.currentTimeMillis()),
                        false,
                        String.valueOf(System.currentTimeMillis())
                );
                mainHandler.post(() -> cb.onResult(aiMsg));
                return;
            }
        }

        // 1-b) 이비인후과/안과 (기타) 키워드
        String[] ent = {
                "눈","시야","충혈","안구","시력","눈물","눈부심","안통","눈통증",
                "귀","이명","청력","코막힘","콧물","재채기","목","목통증","인후통"
        };
        for (String kw : ent) {
            if (norm.contains(kw)) {
                Message aiMsg = new Message(
                        "AI",
                        "전문의 상담이 필요해 보이는 증상입니다.\n" +
                                "내과 또는 외과 중 추가로 원하시는 상담을 입력해주세요. (예: 내과 또는 외과)",
                        Message.formatTimeOnly(System.currentTimeMillis()),
                        false,
                        String.valueOf(System.currentTimeMillis())
                );
                mainHandler.post(() -> cb.onResult(aiMsg));
                return;
            }
        }

        // 2) 그 외는 서버 호출 + 레이블 파싱
        doServerCallAndLabelParsing(patientText, ptTs, cb);
    }

    /**
     * Performs the HTTP POST to SAVE_URL, then parses the "- patient_symptoms", etc.
     */
    private void doServerCallAndLabelParsing(String patientText, String ptTs, Callback cb) {
        String token = new TokenManager(context).getAccessToken();
        JSONObject payload = new JSONObject();
        try {
            payload.put("patient_text", patientText);
        } catch (JSONException e) {
            mainHandler.post(() -> cb.onError(e));
            return;
        }

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(SAVE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> cb.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() ->
                            cb.onError(new IOException("Unexpected code " + response))
                    );
                    return;
                }

                String raw = response.body().string();
                String aiText;
                try {
                    aiText = new JSONObject(raw).optString("ai_text", "").trim();
                } catch (JSONException ex) {
                    mainHandler.post(() -> cb.onError(ex));
                    return;
                }

                // 한글/영문 레이블 정의
                String korP = "- " + context.getString(R.string.label_patient_symptoms);
                String korD = "- " + context.getString(R.string.label_disease_symptoms);
                String korM = "- " + context.getString(R.string.label_main_symptoms);
                String korH = "- " + context.getString(R.string.label_home_actions);
                String korG = "- " + context.getString(R.string.label_guideline);
                String korE = "- " + context.getString(R.string.label_emergency_advice);

                String engP = "- patient_symptoms";
                String engD = "- disease_symptoms";
                String engM = "- main_symptoms";
                String engH = "- home_actions";
                String engG = "- guideline";
                String engE = "- emergency_advice";

                // 파싱 변수 초기화
                String ps = "", ds = "", ms = "", ha = "", gl = "", ea = "";
                for (String line : aiText.split("\\r?\\n")) {
                    line = line.trim();
                    if      (line.matches("(?i)(" + korP + "|" + engP + ")\\s*[:：].*"))
                        ps = line.replaceFirst("(?i).+[:：]\\s*", "");
                    else if (line.matches("(?i)(" + korD + "|" + engD + ")\\s*[:：].*"))
                        ds = line.replaceFirst("(?i).+[:：]\\s*", "");
                    else if (line.matches("(?i)(" + korM + "|" + engM + ")\\s*[:：].*"))
                        ms = line.replaceFirst("(?i).+[:：]\\s*", "");
                    else if (line.matches("(?i)(" + korH + "|" + engH + ")\\s*[:：].*"))
                        ha = line.replaceFirst("(?i).+[:：]\\s*", "");
                    else if (line.matches("(?i)(" + korG + "|" + engG + ")\\s*[:：].*"))
                        gl = line.replaceFirst("(?i).+[:：]\\s*", "");
                    else if (line.matches("(?i)(" + korE + "|" + engE + ")\\s*[:：].*"))
                        ea = line.replaceFirst("(?i).+[:：]\\s*", "");
                }

                // 파싱 실패 시 원본 텍스트만 담기
                boolean allEmpty = ps.isEmpty() && ds.isEmpty() && ms.isEmpty()
                        && ha.isEmpty() && gl.isEmpty() && ea.isEmpty();

                String aiTs = String.valueOf(System.currentTimeMillis());
                Message aiMsg;
                if (allEmpty) {
                    aiMsg = new Message(
                            "AI",
                            aiText,
                            Message.formatTimeOnly(System.currentTimeMillis()),
                            false,
                            aiTs
                    );
                } else {
                    aiMsg = new Message(
                            "AI",
                            aiText,
                            Message.formatTimeOnly(System.currentTimeMillis()),
                            false,
                            aiTs,
                            ps, ds, ms, ha, gl, ea
                    );
                }

                mainHandler.post(() -> cb.onResult(aiMsg));
            }
        });
    }

    /**
     * 진료 완료 후 구분선 API 호출 → SeparatorCallback으로 parts/symptoms 전달
     */
    public void callAddSeparator(String userName, SeparatorCallback cb) {
        String token = new TokenManager(context).getAccessToken();
        Request request = new Request.Builder()
                .url(SEP_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(new byte[0], MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { /* ignore */ }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray pArr = json.optJSONArray("symptom_part");
                    JSONArray dArr = json.optJSONArray("disease_symptoms");
                    ArrayList<String> parts   = new ArrayList<>();
                    ArrayList<String> disease = new ArrayList<>();
                    for (int i = 0; pArr != null && i < pArr.length(); i++)
                        parts.add(pArr.getString(i));
                    for (int i = 0; dArr != null && i < dArr.length(); i++)
                        disease.add(dArr.getString(i));

                    mainHandler.post(() -> cb.onResult(parts, disease));
                } catch (JSONException ignored) {}
            }
        });
    }
}
