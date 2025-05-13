// LlamaActivity.java
package com.example.silmedy.llama;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.View;
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
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;

import java.util.ArrayList;

public class LlamaActivity extends AppCompatActivity {
    private static final String TAG = "LlamaActivity";

    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton btnSend;
    private View loadingIndicator;

    private final ArrayList<Message> msgs = new ArrayList<>();
    private MessageAdapter adapter;
    private LlamaClassifier classifier;
    private boolean isFinished = false;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext())
                .refreshAccessTokenAsync(token -> { /* no-op */ });

        setContentView(R.layout.activity_llama);

        userName   = getIntent().getStringExtra("user_name");
        classifier = new LlamaClassifier(this);

        // 상단바 설정
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent back = new Intent(this, ClinicHomeActivity.class);
            back.putExtra("user_name", userName);
            startActivity(back);
            finish();
        });
        ((TextView)findViewById(R.id.textRoomCode)).setText("닥터링(Dr.Link)");
        ((TextView)findViewById(R.id.textRoomName)).setText("Online");

        // UI 바인딩
        recyclerMessages  = findViewById(R.id.recyclerMessages);
        editMessage       = findViewById(R.id.editMessage);
        btnSend           = findViewById(R.id.btnSend);
        loadingIndicator  = findViewById(R.id.loadingIndicator);

        adapter = new MessageAdapter(this, msgs, userName);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // IME ‘전송’ 버튼 처리
        editMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editMessage.setSingleLine(true);
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER
                            && event.getAction()==KeyEvent.ACTION_DOWN)) {
                if (btnSend.isEnabled()) btnSend.performClick();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(v -> {
            String patientText = editMessage.getText().toString().trim();
            if (patientText.isEmpty()) return;
            editMessage.setText("");

            // 1) 환자 메시지 표시
            String ptTs = String.valueOf(System.currentTimeMillis());
            Message patientMsg = new Message(
                    "나",
                    patientText,
                    Message.formatTimeOnly(System.currentTimeMillis()),
                    false,
                    ptTs
            );
            msgs.add(patientMsg);
            adapter.notifyItemInserted(msgs.size()-1);
            recyclerMessages.scrollToPosition(msgs.size()-1);

            // 2) AI 호출 (서버 + 레이블 파싱)
            loadingIndicator.setVisibility(View.VISIBLE);
            classifier.classifySymptom(patientText, ptTs, new LlamaClassifier.Callback() {
                @Override
                public void onResult(Message aiMsg) {
                    loadingIndicator.setVisibility(View.GONE);
                    msgs.add(aiMsg);
                    adapter.notifyItemInserted(msgs.size()-1);
                    recyclerMessages.scrollToPosition(msgs.size()-1);
                    if (aiMsg.getText().contains("비대면 진료")) {
                        isFinished = true;
                    }
                }
                @Override
                public void onError(Exception e) {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "AI 응답 실패", e);
                }
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinished) {
            classifier.callAddSeparator(userName, (parts, symptoms) -> {
                Intent it = new Intent(LlamaActivity.this, DoctorListActivity.class);
                it.putStringArrayListExtra("part", parts);
                it.putStringArrayListExtra("symptom", symptoms);
                it.putExtra("user_name", userName);
                it.putExtra("department", "내과");
                startActivity(it);
            });
        }
    }
}
