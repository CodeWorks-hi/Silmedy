package com.example.silmedy.llama;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silmedy.R;

public class LlamaActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSend;
    private EditText editMessage;
    private RecyclerView recyclerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        // View 바인딩
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        editMessage = findViewById(R.id.edit_message); // ID 정확히 맞춤
        recyclerMessages = findViewById(R.id.recyclerMessages);

        // 메시지 목록 설정
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        // TODO: 어댑터 설정 필요 시 추가

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 메시지 전송
        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                Log.d("LlamaActivity", "보낸 메시지: " + message);
                // TODO: 메시지를 RecyclerView에 추가하는 코드 추가 필요
                editMessage.setText(""); // 입력창 초기화
            }
        });
    }
}
