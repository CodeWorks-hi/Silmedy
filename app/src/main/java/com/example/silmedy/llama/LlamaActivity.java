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
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.MessageAdapter;
import com.example.silmedy.ui.care_request.DoctorListActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LlamaActivity extends AppCompatActivity {
    private static final String TAG        = "LlamaActivity";
    private static final String COLLECTION = "consult_text";

    private EditText     editMessage;
    private ImageButton  btnSend;
    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;
    private FirebaseFirestore db;
    private String patientId     = "unknown";
    private String prevSymptom   = null;
    public  static ArrayList<String> fullChatHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        // 1) Intent로 전달된 patient_id 가져오기
        Intent intent = getIntent();
        String passedId = intent.getStringExtra("patient_id");
        if (passedId != null && !passedId.isEmpty()) {
            patientId = passedId;
        }
        Log.d(TAG, "PatientId from Intent: " + patientId);

        // 2) 툴바에 사용자 ID 표시
        TextView tvRoomName = findViewById(R.id.textRoomName);
        tvRoomName.setText("ID: " + patientId);

        // 3) Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 4) 뷰 바인딩
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 5) RecyclerView 설정
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 6) Send 버튼 초기 비활성화
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        // 7) 텍스트 입력에 따른 버튼 활성 토글
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Send 클릭 및 엔터키
        btnSend.setOnClickListener(v -> onUserSend());
        editMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editMessage.setSingleLine(true);
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (btnSend.isEnabled()) onUserSend();
                return true;
            }
            return false;
        });
    }

    private void onUserSend() {
        String userText = editMessage.getText().toString().trim();
        if (userText.isEmpty() || patientId.equals("unknown")) return;

        long now = System.currentTimeMillis();
        if (prevSymptom == null) prevSymptom = userText;

        // chatId: 시간 기반으로 유니크하게 생성
        String chatId = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
                .format(new Date(now));

        // (A) Firestore에 환자 질문 저장
        Map<String, Object> entry = new HashMap<>();
        entry.put("patient_text",      userText);
        entry.put("patient_timestamp", now);
        entry.put("ai_text",           "");
        entry.put("ai_timestamp",      null);
        db.collection(COLLECTION)
                .document(patientId)
                .collection("chats")
                .document(chatId)
                .set(entry)
                .addOnSuccessListener(d -> Log.d(TAG, "Saved question chatId=" + chatId))
                .addOnFailureListener(e -> Log.e(TAG, "Save question failed", e));

        // (B) UI에 질문 표시
        messageList.add(new Message("나", userText, now));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);
        fullChatHistory.add("나: " + userText);
        // 날짜 구분선
        messageList.add(Message.createDateSeparator(now));
        adapter.notifyItemInserted(messageList.size() - 1);

        // (C) AI 응답 placeholder
        Message aiPlaceholder = new Message("AI", "", now + 1);
        messageList.add(aiPlaceholder);
        final int aiIndex = messageList.size() - 1;
        adapter.notifyItemInserted(aiIndex);
        recyclerMessages.scrollToPosition(aiIndex);

        // (D) AI 스트리밍 호출 → onComplete에서 답변 저장
        LlamaPromptHelper.sendChatStream(
                patientId,
                prevSymptom,
                userText,
                new LlamaPromptHelper.StreamCallback() {
                    final StringBuilder buffer = new StringBuilder();
                    @Override public void onChunk(String chunk) {
                        buffer.append(chunk);
                        runOnUiThread(() -> {
                            aiPlaceholder.setText(buffer.toString());
                            adapter.notifyItemChanged(aiIndex);
                            recyclerMessages.scrollToPosition(aiIndex);
                        });
                    }
                    @Override public void onComplete() {
                        long aiTs = System.currentTimeMillis();
                        String aiText = buffer.toString();

                        // (E) Firestore에 AI 답변 업데이트
                        Map<String, Object> update = new HashMap<>();
                        update.put("ai_text",      aiText);
                        update.put("ai_timestamp", aiTs);

                        db.collection(COLLECTION)
                                .document(patientId)
                                .collection("chats")
                                .document(chatId)
                                .update(update)
                                .addOnSuccessListener(d -> Log.d(TAG, "Saved answer chatId=" + chatId))
                                .addOnFailureListener(e -> Log.e(TAG, "Save answer failed", e));

                        fullChatHistory.add("AI: " + aiText);
                        if (isPositiveAnswer(aiText)) {
                            startActivity(new Intent(LlamaActivity.this, DoctorListActivity.class));
                            finish();
                        }
                    }
                    @Override public void onError(Exception e) {
                        Log.e(TAG, "Streaming error", e);
                    }
                }
        );

        // (F) 입력창 비우기
        editMessage.setText("");
    }

    private boolean isPositiveAnswer(String text) {
        String lower = text.trim().toLowerCase(Locale.ROOT);
        return lower.contains("예") || lower.contains("네") || lower.contains("진료");
    }
}
