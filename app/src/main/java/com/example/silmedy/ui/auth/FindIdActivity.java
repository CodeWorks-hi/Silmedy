package com.example.silmedy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.PhoneUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FindIdActivity extends AppCompatActivity {

    private EditText editPhone, editCode;
    private TextView textMyId, textMyIdLabel;
    private Button btnVerify, btnCode, btnFindPassword, btnGoToLogin;
    private ImageButton btnBack;

    private final String sentCode = "123456"; // 테스트용 고정 인증 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        // View 바인딩
        editPhone = findViewById(R.id.editPhone);
        editCode = findViewById(R.id.editCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnCode = findViewById(R.id.btnCode);
        textMyId = findViewById(R.id.textMyId);
        textMyIdLabel = findViewById(R.id.textMyIdLabel);
        btnFindPassword = findViewById(R.id.btnFindPassword);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnBack = findViewById(R.id.btnBack);

        // 초기 상태 숨김
        textMyId.setVisibility(View.GONE);
        textMyIdLabel.setVisibility(View.GONE);
        btnFindPassword.setVisibility(View.GONE);
        btnGoToLogin.setVisibility(View.GONE);

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 본인확인 버튼
        btnVerify.setOnClickListener(v -> {
            String rawPhone = editPhone.getText().toString().trim();
            String phone = PhoneUtils.convertToE164Format(rawPhone);
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "연락처를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/request-verification-code";
            JSONObject json = new JSONObject();
            try {
                json.put("phone_number", phone);
            } catch (Exception e) {
                Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(FindIdActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(FindIdActivity.this, "인증 코드가 전송되었습니다", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(FindIdActivity.this, "인증 코드 전송 실패", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });

        // 인증 버튼
        btnCode.setOnClickListener(v -> {
            String code = editCode.getText().toString().trim();
            String rawPhone = editPhone.getText().toString().trim().replace("-", "");

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "인증번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/verify-code-get-email";
            JSONObject json = new JSONObject();
            try {
                json.put("phone_number", rawPhone);
                json.put("code", code);
            } catch (Exception e) {
                Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(FindIdActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> {
                            Toast.makeText(FindIdActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();
                            String email = "";
                            try {
                                JSONObject jsonResponse = new JSONObject(responseData);
                                email = jsonResponse.getString("email");
                            } catch (Exception e) {
                                Log.e("EMAIL EXISTS", "JSON 파싱 오류: " + e.getMessage());
                            }
                            if (email != null) {
                                textMyId.setText(maskEmail(email));
                                textMyId.setVisibility(View.VISIBLE);
                                textMyIdLabel.setVisibility(View.VISIBLE);
                                btnFindPassword.setVisibility(View.VISIBLE);
                                btnGoToLogin.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(FindIdActivity.this, "등록된 아이디가 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(FindIdActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        // 버튼 이벤트들
        btnFindPassword.setOnClickListener(v -> {
            Intent intent = new Intent(FindIdActivity.this, FindPasswordActivity.class);
            startActivity(intent);
            finish();
        });

        btnGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private String maskEmail(String email) {
        int index = email.indexOf("@");
        if (index > 2) {
            return email.substring(0, index - 3) + "**" + email.substring(index - 1);
        } else if (index == 2) {
            return "*" + email.substring(index - 1);
        } else if (index == 1) {
            return "*" + email.substring(index);
        }
        return email;
    }
}