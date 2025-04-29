package com.example.silmedy.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.silmedy.R;
import com.example.silmedy.llama.LlamaActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatSummaryActivity extends AppCompatActivity {

    private TextView textViewSummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_summary);

        // 1) Toolbar 세팅
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_chat_summary);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2) 관리자 체크
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || !"admin@yourdomain.com".equals(user.getEmail())) {
            finish();
            return;
        }

        // 3) 뷰 바인딩
        textViewSummary = findViewById(R.id.textViewSummary);

        // 4) 전체 채팅 원문 가져와 표시
        String chatOriginalText = LlamaActivity.getFullChatSummary();
        Log.d("ChatSummaryActivity", "chatOriginalText=\n" + chatOriginalText);

        if (!chatOriginalText.isEmpty()) {
            textViewSummary.setText(chatOriginalText);
        } else {
            textViewSummary.setText(R.string.hint_no_summary);
        }
    }
}
