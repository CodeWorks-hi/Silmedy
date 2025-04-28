package com.example.silmedy.llama;

import android.content.Intent;
import android.os.Bundle;
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
    private boolean isPositiveAnswer(String userInput) {
        String answer = userInput.trim().toLowerCase();
        return answer.contains("예") || answer.contains("네") || answer.contains("진료") || answer.contains("진료할래요");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        db = FirebaseFirestore.getInstance();

        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> onUserSend());

        editMessage.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                onUserSend();
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

        // 첫 질문 저장
        if (prevSymptom == null) {
            prevSymptom = userText;
        }

        // 1) UI 표시
        Message userMsg = new Message("나", userText, now);
        messageList.add(userMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);

        // 2) 세션에 저장
        DocumentReference qaRef = db.collection(SESSION_COLLECTION)
                .document(sessionId)
                .collection("qa_pairs")
                .document();
        Map<String, Object> qaEntry = new HashMap<>();
        qaEntry.put("question", userText);
        qaEntry.put("answer", null);
        qaEntry.put("timestamp", now);
        qaRef.set(qaEntry);

        // 3) AI 빈 버블 준비
        Message aiPlaceholder = new Message("AI", "", now + 1);
        messageList.add(aiPlaceholder);
        int aiIndex = messageList.size() - 1;
        adapter.notifyItemInserted(aiIndex);
        recyclerMessages.scrollToPosition(aiIndex);

        // 4) AI 스트리밍 호출
        LlamaPromptHelper.sendChatStream(prevSymptom, userText, new LlamaPromptHelper.StreamCallback() {
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

                // --- 이동은 하지 않음 (사용자 입력 기다림)
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Streaming error", e);
            }
        });

        // 5) 입력창 초기화
        editMessage.setText("");

        // 6) 환자가 "예"라고 하면 이동
        if (isPositiveAnswer(userText)) {
            Intent intent = new Intent(LlamaActivity.this, DoctorListActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
