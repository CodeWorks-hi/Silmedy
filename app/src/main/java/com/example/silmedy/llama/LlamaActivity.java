package com.example.silmedy.llama;

import android.os.Bundle;
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

        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        editMessage = findViewById(R.id.editMessage);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // TODO: 메시지 전송 처리
                editMessage.setText("");
            }
        });
    }
}