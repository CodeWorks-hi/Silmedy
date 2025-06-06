package com.example.silmedy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class CheckIdentityActivity extends AppCompatActivity {

    private EditText phoneInput, codeInput;
    private Button verifyButton, codeConfirmButton, confirmButton;
    private ImageView btnBack;

    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_identity);

        mAuth = FirebaseAuth.getInstance();

        // 뷰 바인딩
        phoneInput = findViewById(R.id.editPhone);
        codeInput = findViewById(R.id.editCode);
        verifyButton = findViewById(R.id.btnVerify);
        codeConfirmButton = findViewById(R.id.btnCode);
        confirmButton = findViewById(R.id.btnResult);
        btnBack = findViewById(R.id.btnBack);  // 뒤로가기 버튼 바인딩

        // 인증 코드 전송 버튼
        verifyButton.setOnClickListener(v -> sendVerificationCode());

        // 인증 코드 확인 버튼
        codeConfirmButton.setOnClickListener(v -> verifyCode());

        // 본인 인증 완료 버튼
        confirmButton.setOnClickListener(v -> {
            String rawPhone = phoneInput.getText().toString().trim().replaceAll("-", "");
            String formattedPhone = formatPhoneWithHyphen(rawPhone);
            Intent intent = new Intent(CheckIdentityActivity.this, SignupActivity.class);
            intent.putExtra("contact", formattedPhone);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });

        // 뒤로가기 버튼 → 회원가입 화면으로 이동
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    // 인증 코드 전송 함수
    private void sendVerificationCode() {
        String rawPhone = phoneInput.getText().toString().trim().replaceAll("-", "");
        String phone = rawPhone;

        if (phone.isEmpty() || phone.length() < 9) {
            Toast.makeText(this, "올바른 전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> Toast.makeText(CheckIdentityActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(CheckIdentityActivity.this, "인증 코드가 전송되었습니다", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(CheckIdentityActivity.this, "인증 코드 전송 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 인증 결과 콜백 정의
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                // 자동 인증 성공 시
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        codeInput.setText(code);
                    }
                    signInWithCredential(credential);
                }

                // 인증 실패 시
                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(CheckIdentityActivity.this, "인증 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                // 인증 코드가 사용자에게 전송되었을 때
                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = verifId;
                    resendToken = token;
                    Toast.makeText(CheckIdentityActivity.this, "인증 코드가 전송되었습니다", Toast.LENGTH_SHORT).show();
                }
            };

    // 인증 코드 검증 함수
    private void verifyCode() {
        String code = codeInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim().replaceAll("-", "");

        if (phone.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "전화번호와 인증번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://43.201.73.161:5000/verify-code";
        JSONObject json = new JSONObject();
        try {
            json.put("phone_number", phone);
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
                runOnUiThread(() -> Toast.makeText(CheckIdentityActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(CheckIdentityActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();
                        confirmButton.setEnabled(true);
                        confirmButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CheckIdentityActivity.this, "인증 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Firebase 인증 처리
    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "본인 인증 성공", Toast.LENGTH_SHORT).show();
                        confirmButton.setEnabled(true);
                        confirmButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                    } else {
                        Toast.makeText(this, "인증 실패. 코드 확인", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 전화번호 포맷 (하이픈 삽입)
    private String formatPhoneWithHyphen(String phone) {
        if (phone.length() == 11) {
            return phone.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");
        } else if (phone.length() == 10) {
            return phone.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
        }
        return phone;
    }
}