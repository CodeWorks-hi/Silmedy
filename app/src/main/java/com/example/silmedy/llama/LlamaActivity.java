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

    private final String nickname = "나";

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

        // 메시지 리스트 & 어댑터
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, nickname);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 전송 버튼
        btnSend.setOnClickListener(v -> {
            String userText = editMessage.getText().toString().trim();
            if (userText.isEmpty()) return;

            // 1) 사용자 메시지 추가
            Message userMsg = new Message(nickname, userText, System.currentTimeMillis());
            messageList.add(userMsg);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerMessages.scrollToPosition(messageList.size() - 1);

            // 2) AI 응답 버블 미리 추가
            Message aiMsg = new Message("AI", "", System.currentTimeMillis());
            messageList.add(aiMsg);
            int aiIndex = messageList.size() - 1;
            adapter.notifyItemInserted(aiIndex);
            recyclerMessages.scrollToPosition(aiIndex);

            // 3) 스트리밍 호출 → 메서드 이름을 sendChatStream 으로 변경
            LlamaPromptHelper.sendChatStream(userText, new LlamaPromptHelper.StreamCallback() {
                StringBuilder buffer = new StringBuilder();

                @Override
                public void onChunk(String chunk) {
                    buffer.append(chunk);
                    runOnUiThread(() -> {
                        aiMsg.setText(buffer.toString());
                        adapter.notifyItemChanged(aiIndex);
                        recyclerMessages.scrollToPosition(aiIndex);
                    });
                }

                @Override
                public void onComplete() {
                    // 완료 시 추가 처리 필요하면 여기에
                }

                @Override
                public void onError(Exception e) {
                    Log.e("LlamaActivity", "스트리밍 에러", e);
                }
            });

            // 4) 입력창 초기화
            editMessage.setText("");
        });
    }
}
