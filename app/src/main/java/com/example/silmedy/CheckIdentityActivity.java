package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.ui.login.SignupActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class CheckIdentityActivity extends AppCompatActivity {

    private EditText phoneInput, codeInput;
    private Button verifyButton, codeConfirmButton, confirmButton;

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
        confirmButton = findViewById(R.id.btnConfirm);

        verifyButton.setOnClickListener(v -> sendVerificationCode());
        codeConfirmButton.setOnClickListener(v -> verifyCode());
        confirmButton.setOnClickListener(v -> {
            String rawPhone = phoneInput.getText().toString().trim().replaceAll("-", "");
            String formattedPhone = formatPhoneWithHyphen(rawPhone);
            Intent intent = new Intent(CheckIdentityActivity.this, SignupActivity.class);
            intent.putExtra("phoneNumber", formattedPhone);
            startActivity(intent);
            finish();
        });
    }

    private void sendVerificationCode() {
        String rawPhone = phoneInput.getText().toString().trim();
        String cleanedPhone = rawPhone.replaceAll("-", "");  // 하이픈 제거
        String phone = cleanedPhone;

        if (phone.isEmpty() || phone.length() < 9) {
            Toast.makeText(this, "올바른 전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedPhone = phone.startsWith("+82") ? phone : "+82" + phone.replaceFirst("^0", "");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(formattedPhone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        codeInput.setText(code); // 자동 입력
                    }
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(CheckIdentityActivity.this, "인증 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = verifId;
                    resendToken = token;
                    Toast.makeText(CheckIdentityActivity.this, "인증 코드가 전송되었습니다", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyCode() {
        String code = codeInput.getText().toString().trim();

        if (verificationId == null || code.isEmpty()) {
            Toast.makeText(this, "인증 코드 전송을 먼저 해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "본인 인증 성공", Toast.LENGTH_SHORT).show();
                        confirmButton.setEnabled(true); // 본인 인증 완료 버튼 활성화
                        confirmButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark
                        )); // 버튼 색상 변경
                    } else {
                        Toast.makeText(this, "인증 실패. 코드 확인", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatPhoneWithHyphen(String phone) {
        // "01012345678" → "010-1234-5678"
        if (phone.length() == 11) {
            return phone.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");
        } else if (phone.length() == 10) {
            return phone.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
        }
        return phone;  // 길이가 이상할 경우 그대로 반환
    }
}
