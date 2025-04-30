package com.example.silmedy.llama;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.MessageAdapter;
import com.example.silmedy.llama.LlamaClassifier;
import com.example.silmedy.llama.LlamaClassifier.ClassificationCallback;
import com.example.silmedy.llama.LlamaClassifier.LlamaPromptHelper;
import com.example.silmedy.llama.LlamaClassifier.LlamaPromptHelper.StreamCallback;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.photo_clinic.BodyMain;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * LlamaActivity.java
 * • layout/activity_llama.xml 기반 UI
 * • 로그인된 사용자의 이메일을 sanitized userId로 사용
 * • 외과/내과 분류 → BodyMain 또는 LlamaPromptHelper 호출
 * • Cloud Firestore에 Q&A 저장 및 리스닝
 */
public class LlamaActivity extends AppCompatActivity {
    private static final String TAG = "LlamaActivity";

    private String userId;

    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton btnSend;
    private MessageAdapter adapter;
    private final List<Message> msgs = new ArrayList<>();

    private final LlamaClassifier classifier = new LlamaClassifier();
    private String prevSymptom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_llama);

        // 2) UI 바인딩
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.textRoomCode)).setText("닥터링(Dr.Link)");
        ((TextView)findViewById(R.id.textRoomName)).setText("AI 의료 연결자");

        recyclerMessages = findViewById(R.id.recyclerMessages);
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);

        // 3) RecyclerView 설정
        adapter = new MessageAdapter(this, msgs, userId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 4) EditText 동작 제어
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){
                boolean has = s.toString().trim().length()>0;
                btnSend.setEnabled(has);
                btnSend.setAlpha(has?1f:0.5f);
            }
            @Override public void afterTextChanged(Editable s){}
        });
        editMessage.setSingleLine(true);
        editMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN)) {
                if (btnSend.isEnabled()) btnSend.performClick();
                return true;
            }
            return false;
        });

        // 5) Send 버튼 클릭
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            editMessage.setText("");
            classifyOrAnalyze(text);
        });
    }

    /** 외과/내과 분류 or 내과 증상 분석 흐름 */
    private void classifyOrAnalyze(String text) {
        String ptTs = String.valueOf(System.currentTimeMillis());

        classifier.classifyOrPrompt(text, new ClassificationCallback() {
            @Override public void onSurgicalQuestion(String prompt) {
                runOnUiThread(() -> showSurgicalDialog(text, prompt));
            }
            @Override public void onClassification(String category) {
                runOnUiThread(() -> {
                    if ("외과".equals(category)) {
                        showSurgicalDialog(text,
                                "외과 진료가 필요해 보입니다. 신체 부위 선택·촬영 페이지로 이동하시겠습니까?");
                    } else {
                        Log.d(TAG, "내과 분류 - 응답 표시");
                        saveChat(text, ptTs, "", "");
                    }
                });
            }
            @Override public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(LlamaActivity.this,
                                "분류 오류", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /** 외과 촬영 페이지 이동 다이얼로그 */
    private void showSurgicalDialog(String patientText, String prompt) {
        new AlertDialog.Builder(this)
                .setMessage(prompt)
                .setPositiveButton("예", (d,w) -> {
                    startActivity(new Intent(this, BodyMain.class));
                })
                .setNegativeButton("아니오", (d,w) -> {
                    // 외과가 아니라는 판단 후, 바로 사용자 메시지만 표시 (AI 분석은 하지 않음)
                    runOnUiThread(() -> {
                        String ptTs = String.valueOf(System.currentTimeMillis());
                        msgs.add(new Message("나", patientText, formatTimeOnly(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())), false, ptTs));
                        adapter.notifyItemInserted(msgs.size() - 1);
                        recyclerMessages.scrollToPosition(msgs.size() - 1);
                    });
                })
                .show();
    }

    /** API를 통해 Q&A 저장 */
    private void saveChat(String pt, String ptTs, String ai, String aiTs) {
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        try {
            // Add created_at before creating JSON object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String createdAt = sdf.format(new Date());

            JSONObject json = new JSONObject();
            json.put("patient_text", pt);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("http://43.201.73.161:5000/chat/save")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Save chat failed", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String resStr = response.body().string();
                        JSONObject resJson = null;
                        try {
                            resJson = new JSONObject(resStr);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String aiText = resJson.optString("ai_text", "").trim();

                        runOnUiThread(() -> {
                            // 1. 사용자 메시지 표시
                            msgs.add(new Message("나", pt, formatTimeOnly(createdAt), false, ptTs));
                            adapter.notifyItemInserted(msgs.size() - 1);
                            recyclerMessages.scrollToPosition(msgs.size() - 1);

                            // 2. AI 응답 메시지 표시
                            if (!aiText.isEmpty()) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    msgs.add(new Message("AI", aiText, formatTimeOnly(createdAt), false, aiTs));
                                    adapter.notifyItemInserted(msgs.size() - 1);
                                    recyclerMessages.scrollToPosition(msgs.size() - 1);
                                }, 800);
                            }
                        });

                        Log.d(TAG, "Chat saved to API and displayed");
                    } else {
                        Log.e(TAG, "API save failed: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "JSON creation failed", e);
        }
    }

    private String formatTimeOnly(String fullTimestamp) {
        try {
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = fullFormat.parse(fullTimestamp);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return timeFormat.format(date);
        } catch (Exception e) {
            return fullTimestamp;
        }
    }
}

