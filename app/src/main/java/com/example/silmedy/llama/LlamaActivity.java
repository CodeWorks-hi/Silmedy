package com.example.silmedy.llama;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silmedy.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class LlamaActivity extends AppCompatActivity {
    private EditText editMessage;
    private Button btnSend;
    private static final String API_KEY = "";
    private static final String API_URL = "https://api-inference.huggingface.co/v1/chat/completions";

    private RecyclerView recyclerMessages;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList = new ArrayList<>();
    private String nickname = "나";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String userMessage = editMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                Message msg = new Message(nickname, userMessage, System.currentTimeMillis());
                messageList.add(msg);
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerMessages.scrollToPosition(messageList.size() - 1);
                sendRequest(userMessage);
                editMessage.setText("");
            }
        });
    }

    private void sendRequest(String messageText) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", messageText);

            JSONArray messagesArray = new JSONArray();
            messagesArray.put(message);

            JSONObject requestBodyJson = new JSONObject();
            requestBodyJson.put("provider", "novita");
            requestBodyJson.put("model", "meta-llama/Llama-3.1-8B-Instruct");
            requestBodyJson.put("messages", messagesArray);
            requestBodyJson.put("max_tokens", 512);

            RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            JSONArray choices = json.getJSONArray("choices");
                            String reply = choices.getJSONObject(0).getJSONObject("message").getString("content");
                            Message aiMsg = new Message("AI", reply.trim(), System.currentTimeMillis());
                            runOnUiThread(() -> {
                                messageList.add(aiMsg);
                                adapter.notifyItemInserted(messageList.size() - 1);
                                recyclerMessages.scrollToPosition(messageList.size() - 1);
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}