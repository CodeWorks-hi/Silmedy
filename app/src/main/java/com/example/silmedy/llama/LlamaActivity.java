package com.example.silmedy.llama;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silmedy.R;
import java.util.ArrayList;

public class LlamaActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSend;
    private EditText editMessage;
    private RecyclerView recyclerMessages;

    // 추가 필드
    private ArrayList<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;
    private String nickname = "환자"; // 또는 Intent/SharedPreferences에서 가져올 수 있음

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        editMessage = findViewById(R.id.edit_message);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(nickname, text, System.currentTimeMillis());
                msg.setProfileImageUrl(null);
                msg.setImage(false);
                messageList.add(msg);
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerMessages.scrollToPosition(messageList.size() - 1);
                editMessage.setText("");
            }
        });
    }
}
