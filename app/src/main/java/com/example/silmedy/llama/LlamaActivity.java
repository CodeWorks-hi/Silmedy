package com.example.silmedy.llama;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.BuildConfig;
import com.example.silmedy.llama.Message;
import com.example.silmedy.adapter.MessageAdapter;
import com.example.silmedy.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LlamaActivity extends AppCompatActivity {

    public EditText editMessage;
    public ImageButton btnSend;
    public RecyclerView recyclerMessages;
    public MessageAdapter adapter;
    public ArrayList<Message> messageList;

    public final String nickname = "나";
    public static final String API_KEY = BuildConfig.HUGGINGFACE_API_KEY;
    public static final String API_URL = BuildConfig.HUGGINGFACE_API_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llama);

        editMessage       = findViewById(R.id.editMessage);
        btnSend           = findViewById(R.id.btnSend);
        recyclerMessages  = findViewById(R.id.recyclerMessages);

        // 메시지 리스트 & 어댑터 초기화
        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(this, messageList, nickname);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        // 전송 버튼 클릭 → 메시지 추가 + API 요청
        btnSend.setOnClickListener(v -> {
            String userText = editMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                // 1) 사용자 메시지 추가
                Message userMsg = new Message(nickname, userText, System.currentTimeMillis());
                messageList.add(userMsg);
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerMessages.scrollToPosition(messageList.size() - 1);

                // 2) AI 요청
                sendRequest(userText);

                // 3) 입력창 초기화
                editMessage.setText("");
            }
        });
    }

    private void sendRequest(String messageText) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            // 요청 페이로드 생성
            JSONObject userObj = new JSONObject()
                    .put("role", "user")
                    .put("content", messageText);

            JSONArray messagesArray = new JSONArray().put(userObj);

            JSONObject bodyJson = new JSONObject()
                    .put("provider", "novita")
                    .put("model", "meta-llama/Llama-3.1-8B-Instruct")
                    .put("messages", messagesArray)
                    .put("max_tokens", 512);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("LlamaActivity", "API 요청 실패", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("LlamaActivity", "API 응답 오류: " + response.code());
                        return;
                    }
                    String resp = response.body().string();
                    try {
                        JSONObject json = new JSONObject(resp);
                        JSONArray choices = json.getJSONArray("choices");
                        String reply = choices
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // UI 스레드에서 AI 메시지 추가
                        runOnUiThread(() -> {
                            Message aiMsg = new Message("AI", reply, System.currentTimeMillis());
                            messageList.add(aiMsg);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerMessages.scrollToPosition(messageList.size() - 1);
                        });
                    } catch (Exception e) {
                        Log.e("LlamaActivity", "응답 파싱 실패", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("LlamaActivity", "요청 구성 실패", e);
        }
    }
}
