package com.example.silmedy.llama;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.MessageAdapter;

import java.util.ArrayList;

public class LlamaActivity extends AppCompatActivity {

    private EditText editMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        // 뒤로가기
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 뷰 초기화
        editMessage      = findViewById(R.id.editMessage);
        btnSend          = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        // 리스트 & 어댑터
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, "나");
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String userText = editMessage.getText().toString().trim();
            if (userText.isEmpty()) return;

            // 사용자 메시지
            Message userMsg = new Message("나", userText, System.currentTimeMillis());
            messageList.add(userMsg);
            adapter.notifyItemInserted(messageList.size()-1);

            // AI 빈 버블 추가
            Message aiMsg = new Message("AI", "", System.currentTimeMillis());
            messageList.add(aiMsg);
            int aiIndex = messageList.size()-1;
            adapter.notifyItemInserted(aiIndex);

            // 스트리밍 호출
            LlamaPromptHelper.sendChatStream(userText, new LlamaPromptHelper.StreamCallback() {
                StringBuilder buf = new StringBuilder();

                @Override
                public void onChunk(String chunk) {
                    buf.append(chunk);
                    runOnUiThread(() -> {
                        aiMsg.setText(buf.toString());
                        adapter.notifyItemChanged(aiIndex);
                    });
                }

                @Override
                public void onComplete() { /* 필요시 후처리 */ }

                @Override
                public void onError(Exception e) {
                    Log.e("LlamaActivity", "스트리밍 에러", e);
                }
            });

            editMessage.setText("");
        });
    }
}
