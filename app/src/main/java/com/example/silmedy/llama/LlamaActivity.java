package com.example.silmedy.llama;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private FirebaseFirestore db;
    private CollectionReference chatRef;

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
        setContentView(R.layout.activity_llama);

        // 1) 로그인 사용자 토큰 → userId
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this,
                    "유효한 로그인 정보가 없습니다. 로그인 후 사용해주세요.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = accessToken;
        // 2) Firestore 초기화
        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("consult_text")
                .document(userId)
                .collection("chats");

        // 3) UI 바인딩
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.textRoomCode)).setText("Slimedy");
        ((TextView)findViewById(R.id.textRoomName)).setText("의료 상담 AI 챗봇");

        recyclerMessages = findViewById(R.id.recyclerMessages);
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);

        // 4) RecyclerView 설정
        adapter = new MessageAdapter(this, msgs, userId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 5) Firestore 리스닝
        chatRef.orderBy("patient_timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Firestore listen error", e);
                        return;
                    }
                    msgs.clear();
                    long lastDate = 0;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        long ptTs = doc.getLong("patient_timestamp");
                        String pt  = doc.getString("patient_text");
                        long aiTs  = doc.getLong("ai_timestamp");
                        String ai  = doc.getString("ai_text");

                        if (!Message.isSameDay(lastDate, ptTs)) {
                            msgs.add(Message.createDateSeparator(ptTs));
                            lastDate = ptTs;
                        }
                        // 환자 메시지 객체 생성 및 추가
                        Message patientMsg = new Message();
                        patientMsg.setChat_id(String.valueOf(ptTs));
                        patientMsg.setPatientId(userId);
                        patientMsg.setText(pt);
                        patientMsg.setIs_separator(false);
                        patientMsg.setCreated_at(
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date(ptTs))
                        );
                        msgs.add(patientMsg);

                        // AI 메시지 객체 생성 및 추가
                        Message aiMsg = new Message();
                        aiMsg.setChat_id(String.valueOf(aiTs));
                        aiMsg.setPatientId("AI");
                        aiMsg.setText(ai);
                        aiMsg.setIs_separator(false);
                        aiMsg.setCreated_at(
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date(aiTs))
                        );
                        msgs.add(aiMsg);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(msgs.size() - 1);
                });

        // 6) EditText 동작 제어
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
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

        // 7) Send 버튼 클릭
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            editMessage.setText("");
            classifyOrAnalyze(text);
        });
    }

    /** 외과/내과 분류 or 내과 증상 분석 흐름 */
    private void classifyOrAnalyze(String text) {
        long ptTs = System.currentTimeMillis();
        saveChat(text, ptTs, "", 0L);

        classifier.classifyOrPrompt(text, new ClassificationCallback() {
            @Override public void onSurgicalQuestion(String prompt) {
                runOnUiThread(() -> showSurgicalDialog(text, prompt));
            }
            @Override public void onClassification(String category) {
                runOnUiThread(() -> {
                    if ("외과".equals(category)) {
                        showSurgicalDialog(text,
                                "외과 진료가 필요해 보입니다. 터치로 증상확인 페이지로 이동하시겠습니까?");
                    } else {
                        sendInternalChat(text, ptTs);
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

    /** 내과 AI 응답 스트리밍 및 저장 */
    private void sendInternalChat(String patientText, long ptTs) {
        LlamaPromptHelper.sendChatStream(
                userId, prevSymptom, patientText,
                new StreamCallback() {
                    StringBuilder aiBuf = new StringBuilder();
                    @Override public void onChunk(String chunk) {
                        aiBuf.append(chunk);
                    }
                    @Override public void onComplete() {
                        long aiTs = System.currentTimeMillis();
                        saveChat(patientText, ptTs, aiBuf.toString().trim(), aiTs);
                        if (prevSymptom.isEmpty()) prevSymptom = patientText;
                    }
                    @Override public void onError(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(LlamaActivity.this,
                                        "AI 오류", Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    /** 외과 촬영 페이지 이동 다이얼로그 */
    private void showSurgicalDialog(String patientText, String prompt) {
        new AlertDialog.Builder(this)
                .setMessage(prompt)
                .setPositiveButton("예", (d,w) -> {
                    startActivity(new Intent(this, BodyMain.class));
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    /** Firestore에 Q&A 저장 */
    private void saveChat(String pt, long ptTs, String ai, long aiTs) {
        Map<String,Object> data = new HashMap<>();
        data.put("patient_text", pt);
        data.put("patient_timestamp", ptTs);
        data.put("ai_text", ai);
        data.put("ai_timestamp", aiTs);

        chatRef.document(String.valueOf(ptTs))
                .set(data)
                .addOnFailureListener(e -> Log.e(TAG, "Save error", e));
    }
}
