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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.MessageAdapter;
import com.example.silmedy.ui.care_request.DoctorListActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LlamaActivity extends AppCompatActivity {

    private static final String TAG = "LlamaActivity";
    private static final String SESSION_COLLECTION = "consult_sessions";

    private EditText editMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;
    private FirebaseFirestore db;

    private String sessionId = null;
    private String prevSymptom = null;
    private String userId = null; // Firebase Auth 에서 가져올 userId

    private boolean isPositiveAnswer(String userInput) {
        String answer = userInput.trim().toLowerCase();
        return answer.contains("예") || answer.contains("네") || answer.contains("진료") || answer.contains("진료할래요");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        db = FirebaseFirestore.getInstance();

        // userId 초기화 (필요시 FirebaseAuth 연동)
        // userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Send 버튼 활성화/비활성화
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSend.setOnClickListener(v -> onUserSend());

        editMessage.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
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
        if (userText.isEmpty()) return;

        if (sessionId == null) {
            db.collection(SESSION_COLLECTION)
                    .add(new HashMap<>())
                    .addOnSuccessListener(doc -> {
                        sessionId = doc.getId();
                        Log.d(TAG, "Session created: " + sessionId);
                        continueConversation(userText);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Session create failed", e));
        } else {
            continueConversation(userText);
        }
    }

    private void continueConversation(String userText) {
        long now = System.currentTimeMillis();

        if (prevSymptom == null) {
            prevSymptom = userText;
        }

        // 사용자 메시지 표시
        Message userMsg = new Message("나", userText, now);
        messageList.add(userMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);

        // Firestore QA 초기 저장
        DocumentReference qaRef = db.collection(SESSION_COLLECTION)
                .document(sessionId)
                .collection("qa_pairs")
                .document();
        Map<String, Object> qaEntry = new HashMap<>();
        qaEntry.put("question", userText);
        qaEntry.put("answer", null);
        qaEntry.put("timestamp", now);
        qaRef.set(qaEntry);

        // AI 버블 추가
        Message aiPlaceholder = new Message("AI", "", now + 1);
        messageList.add(aiPlaceholder);
        int aiIndex = messageList.size() - 1;
        adapter.notifyItemInserted(aiIndex);
        recyclerMessages.scrollToPosition(aiIndex);

        // AI 스트리밍 호출 (userId 추가)
        LlamaPromptHelper.sendChatStream(
                userId,
                prevSymptom,
                userText,
                new LlamaPromptHelper.StreamCallback() {
                    final StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onChunk(String chunk) {
                        buffer.append(chunk);
                        runOnUiThread(() -> {
                            aiPlaceholder.setText(buffer.toString());
                            adapter.notifyItemChanged(aiIndex);
                            recyclerMessages.scrollToPosition(aiIndex);
                        });
                    }

                    @Override
                    public void onComplete() {
                        long aiTs = System.currentTimeMillis();
                        Map<String, Object> update = new HashMap<>();
                        update.put("answer", buffer.toString());
                        update.put("ai_timestamp", aiTs);
                        qaRef.update(update);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Streaming error", e);
                    }
                }
        );

        // 입력창 초기화
        editMessage.setText("");

        // 긍정 답변인 경우 진료 예약 화면으로 이동
        if (isPositiveAnswer(userText)) {
            startActivity(new Intent(LlamaActivity.this, DoctorListActivity.class));
            finish();
        }
    }
}
