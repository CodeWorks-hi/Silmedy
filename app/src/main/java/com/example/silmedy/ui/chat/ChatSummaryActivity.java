package com.example.silmedy.ui.chat;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.silmedy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.silmedy.llama.LlamaActivity;

public class ChatSummaryActivity extends AppCompatActivity {

    private TextView textViewSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_summary);

        // 관리자 인증
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || !user.getEmail().equals("admin@yourdomain.com")) {
            finish(); // 관리자가 아니면 바로 종료
            return;
        }

        textViewSummary = findViewById(R.id.textViewSummary);

        // LlamaActivity로부터 채팅 원문 가져오기
        String chatOriginalText = LlamaActivity.getFullChatSummary();  // <- 여기!

        if (chatOriginalText != null && !chatOriginalText.isEmpty()) {
            textViewSummary.setText(chatOriginalText);
        } else {
            textViewSummary.setText(getString(R.string.hint_no_summary));  // 요약 없을 때 기본 메시지
        }
    }
}
