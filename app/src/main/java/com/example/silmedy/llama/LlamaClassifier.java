// LlamaClassifier.java
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
     * 1) patientText POST → 2) ai_text 응답 → 3) 레이블 파싱 → 4) Message 콜백
     * Authorization 헤더 추가
     */
    public void classifySymptom(String patientText, String ptTs, Callback cb) {
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

// 한글 레이블
                String korP = "- " + context.getString(R.string.label_patient_symptoms);   // "- 환자 증상"
                String korD = "- " + context.getString(R.string.label_disease_symptoms);
                String korM = "- " + context.getString(R.string.label_main_symptoms);
                String korH = "- " + context.getString(R.string.label_home_actions);
                String korG = "- " + context.getString(R.string.label_guideline);
                String korE = "- " + context.getString(R.string.label_emergency_advice);
// 영문 레이블
                String engP = "- patient_symptoms";
                String engD = "- disease_symptoms";
                String engM = "- main_symptoms";
                String engH = "- home_actions";
                String engG = "- guideline";
                String engE = "- emergency_advice";

                String ps="", ds="", ms="", ha="", gl="", ea="";

                for (String line : aiText.split("\\r?\\n")) {
                    line = line.trim();
                    if      (line.startsWith(korP) || line.startsWith(engP)) ps = line.substring(line.indexOf(':')+1).trim();
                    else if (line.startsWith(korD) || line.startsWith(engD)) ds = line.substring(line.indexOf(':')+1).trim();
                    else if (line.startsWith(korM) || line.startsWith(engM)) ms = line.substring(line.indexOf(':')+1).trim();
                    else if (line.startsWith(korH) || line.startsWith(engH)) ha = line.substring(line.indexOf(':')+1).trim();
                    else if (line.startsWith(korG) || line.startsWith(engG)) gl = line.substring(line.indexOf(':')+1).trim();
                    else if (line.startsWith(korE) || line.startsWith(engE)) ea = line.substring(line.indexOf(':')+1).trim();
                }


                String aiTs = String.valueOf(System.currentTimeMillis());
                Message aiMsg = new Message(
                        "AI",
                        aiText,
                        Message.formatTimeOnly(System.currentTimeMillis()),
                        false,
                        aiTs,
                        ps, ds, ms, ha, gl, ea
                );

                mainHandler.post(() -> cb.onResult(aiMsg));
            }
        });
    }

    /**
     * 진료 종료 후 구분선 API 호출 → SeparatorCallback 으로 parts/symptoms 전달
     * Authorization 헤더 추가
     */
    public void callAddSeparator(String userName, SeparatorCallback cb) {
        String token = new TokenManager(context).getAccessToken();

        Request request = new Request.Builder()
                .url(SEP_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(new byte[0], MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { /* 무시 */ }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
                } catch (JSONException e) {
                    // ignore
                }
            }
        });
    }
}
