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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private FirebaseFirestore  db;
    private String userId      = "unknown";
    private String prevSymptom = null;
    public  static ArrayList<String> fullChatHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        // Initialize Firestore & Auth
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            Log.d(TAG, "User logged in: " + userId);
        }

        // Display userId in toolbar
        TextView tvRoomName = findViewById(R.id.textRoomName);
        tvRoomName.setText("ID: " + userId);

        // View binding
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // RecyclerView setup
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // Disable send by default
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        // Enable/disable send based on input
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Send click & enter key
        btnSend.setOnClickListener(v -> onUserSend());
        editMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editMessage.setSingleLine(true);
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (btnSend.isEnabled()) btnSend.performClick();
                return true;
            }
            return false;
        });
    }

    private void onUserSend() {
        String userText = editMessage.getText().toString().trim();
        if (userText.isEmpty() || userId.equals("unknown")) return;

        long now = System.currentTimeMillis();
        if (prevSymptom == null) prevSymptom = userText;

        // Save patient message
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("sender", "patient");
        patientData.put("text", userText);
        patientData.put("timestamp", now);
        db.collection(COLLECTION)
                .document(userId)
                .collection("chats")
                .document(String.valueOf(now))
                .set(patientData)
                .addOnSuccessListener(d -> Log.d(TAG, "Saved patient message"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving patient message", e));

        // Show patient message
        messageList.add(new Message("나", userText, now));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);
        fullChatHistory.add("나: " + userText);

        // Date separator
        messageList.add(Message.createDateSeparator(now));
        adapter.notifyItemInserted(messageList.size() - 1);

        // AI placeholder
        Message aiPlaceholder = new Message("AI", "", now + 1);
        messageList.add(aiPlaceholder);
        final int aiIndex = messageList.size() - 1;
        adapter.notifyItemInserted(aiIndex);
        recyclerMessages.scrollToPosition(aiIndex);

        // AI stream
        LlamaPromptHelper.sendChatStream(
                userId,
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

                        // Save AI message
                        Map<String, Object> aiData = new HashMap<>();
                        aiData.put("sender", "AI");
                        aiData.put("text", aiText);
                        aiData.put("timestamp", aiTs);
                        db.collection(COLLECTION)
                                .document(userId)
                                .collection("chats")
                                .document(String.valueOf(aiTs))
                                .set(aiData)
                                .addOnSuccessListener(d -> Log.d(TAG, "Saved AI message"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error saving AI message", e));

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

        // Clear input
        editMessage.setText("");
    }

    private boolean isPositiveAnswer(String text) {
        String lower = text.trim().toLowerCase(Locale.ROOT);
        return lower.contains("예") || lower.contains("네") || lower.contains("진료");
    }
}
