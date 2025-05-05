package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.auth.LoginActivity;
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

public class ChangePasswordActivity extends AppCompatActivity {

    // For manual verification/testing without Firebase
    private final String sentCode = "123456"; // 임시비밀번호 고정값

    EditText editEmail;
    EditText editPhone, editCode;
    Button btnVerifyPhone, btnVerifyCode;
    View inputCodeLayout, newPasswordSection;

    EditText editNewPassword, editConfirmPassword;
    Button btnChangePassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // View 연결
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnVerifyPhone = findViewById(R.id.btnVerify);
        editCode = findViewById(R.id.editCode);
        btnVerifyCode = findViewById(R.id.btnCode);

        inputCodeLayout = findViewById(R.id.codeBlock);
        newPasswordSection = findViewById(R.id.inputNewPasswordLayout);
        newPasswordSection.setVisibility(View.GONE);
        findViewById(R.id.labelNewPassword).setVisibility(View.GONE);
        findViewById(R.id.labelConfirmPassword).setVisibility(View.GONE);
        findViewById(R.id.inputConfirmPasswordLayout).setVisibility(View.GONE);
        findViewById(R.id.btnChangePassword).setVisibility(View.GONE);

        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        btnChangePassword = findViewById(R.id.btnChangePassword);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ChangePasswordActivity.this, MyPageActivity.class);
            startActivity(backIntent);
            finish();
        });

        // 본인확인 버튼 클릭
        btnVerifyPhone.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }
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
                    runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "인증 코드가 전송되었습니다", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "인증 코드 전송 실패", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });

        // 인증 버튼 클릭
        btnVerifyCode.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String code = editCode.getText().toString().trim();
            String rawPhone = editPhone.getText().toString().trim().replace("-", "");

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "인증번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/verify-code-check-user";
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
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
                    runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();
                            // TODO: parse JSON and set email field if needed
                            // 예시: editEmail.setText(parsedEmail);
                            // 새 비밀번호 영역 표시
                            findViewById(R.id.labelNewPassword).setVisibility(View.VISIBLE);
                            newPasswordSection.setVisibility(View.VISIBLE);
                            findViewById(R.id.labelConfirmPassword).setVisibility(View.VISIBLE);
                            findViewById(R.id.inputConfirmPasswordLayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.btnChangePassword).setVisibility(View.VISIBLE);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        // 비밀번호 변경 버튼 클릭
        btnChangePassword.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String pw = editNewPassword.getText().toString();
            String pwConfirm = editConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(pw) || TextUtils.isEmpty(pwConfirm)) {
                Toast.makeText(this, "모든 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(pwConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // 비밀번호는 소문자와 숫자를 포함한 6자 이상이어야 함
            if (!pw.matches("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{6,}$")) {
                Toast.makeText(this, "비밀번호는 소문자와 숫자를 포함한 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/patient/repassword";
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
                json.put("new_password", pw);
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
                    runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show());
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChangePasswordActivity.this, "비밀번호 변경 실패", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });
    }
}
