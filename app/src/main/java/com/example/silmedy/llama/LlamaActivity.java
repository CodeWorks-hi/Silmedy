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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silmedy.R;
import com.example.silmedy.adapter.MessageAdapter;
import com.example.silmedy.ui.care_request.DoctorListActivity;
import com.google.firebase.auth.FirebaseAuth;
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

    private String userId      = "unknown";
    private String prevSymptom = null;
    public  static ArrayList<String> fullChatHistory = new ArrayList<>();

    private EditText     editMessage;
    private ImageButton  btnSend;
    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;
    private FirebaseFirestore  db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        // Firestore & Auth 초기화
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            Log.d(TAG, "Logged in as: " + userId);
        } else {
            Log.w(TAG, "No user logged in");
        }

        // 뷰 바인딩
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        // RecyclerView 세팅 (변경 없음)
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 뒤로가기
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 1) 입력값에 따라 Send 버튼 활성화
        btnSend.setEnabled(false);
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                btnSend.setEnabled(s.toString().trim().length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 2) 키보드 엔터를 Send로
        editMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editMessage.setSingleLine(true);
        editMessage.setOnEditorActionListener((TextView v, int actionId, KeyEvent ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER && ev.getAction() == KeyEvent.ACTION_DOWN)) {
                if (btnSend.isEnabled()) btnSend.performClick();
                return true;
            }
            return false;
        });

        // 3) 전송 버튼 클릭
        btnSend.setOnClickListener(v -> onUserSend());
    }

    private void onUserSend() {
        String userText = editMessage.getText().toString().trim();
        if (userText.isEmpty() || userId.equals("unknown")) return;

        long now = System.currentTimeMillis();
        if (prevSymptom == null) prevSymptom = userText;

        // (A) 사용자 메시지 추가
        messageList.add(new Message("나", userText, now));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);
        fullChatHistory.add("나: " + userText);

        // (B) 날짜 구분선 추가
        messageList.add(Message.createDateSeparator(now));
        adapter.notifyItemInserted(messageList.size() - 1);

        // (C) AI 응답 placeholder
        Message aiPlaceholder = new Message("AI", "", now + 1);
        messageList.add(aiPlaceholder);
        final int aiIndex = messageList.size() - 1;
        adapter.notifyItemInserted(aiIndex);
        recyclerMessages.scrollToPosition(aiIndex);

        // (D) Stream 요청
        LlamaPromptHelper.sendChatStream(
                userId, prevSymptom, userText,
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
                        saveConsultData(userText, aiText, aiTs);
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

        // (E) 입력창 비우기
        editMessage.setText("");
    }

    private boolean isPositiveAnswer(String txt) {
        txt = txt.toLowerCase(Locale.ROOT);
        return txt.contains("예") || txt.contains("네") || txt.contains("진료");
    }

    private void saveConsultData(String patientText, String aiText, long ts) {
        String chatId = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
                .format(new Date(ts));
        Map<String,Object> data = new HashMap<>();
        data.put("chat_id",    chatId);
        data.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(ts)));
        data.put("is_separater", false);
        data.put("patient_id",   userId);
        data.put("patient_text", patientText);
        data.put("ai_text",      aiText);

        db.collection(COLLECTION)
                .document(userId)
                .collection("chats")
                .document(chatId)
                .set(data)
                .addOnSuccessListener(r -> Log.d(TAG, "Saved Q&A"))
                .addOnFailureListener(e -> Log.e(TAG, "Save failed", e));
    }

    public static String getFullChatSummary() {
        if (fullChatHistory.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String line : fullChatHistory) sb.append(line).append("\n");
        return sb.toString();
    }
}
