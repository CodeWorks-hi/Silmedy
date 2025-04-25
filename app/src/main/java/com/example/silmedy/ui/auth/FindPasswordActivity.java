package com.example.silmedy.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.google.firebase.firestore.FirebaseFirestore;

class PhoneUtils {
    public static String convertToE164Format(String phone) {
        String sanitized = phone.replace("-", "");
        if (sanitized.startsWith("0")) {
            return "+82" + sanitized.substring(1);
        } else {
            return sanitized;
        }
    }
}

public class FindPasswordActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_find_password);

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
        btnBack.setOnClickListener(v -> finish());

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

            // 실제로는 서버에서 인증번호 발송 로직 필요
            Toast.makeText(this, "인증번호가 발송되었습니다", Toast.LENGTH_SHORT).show();
            inputCodeLayout.setVisibility(View.VISIBLE);
            btnVerifyCode.setVisibility(View.VISIBLE);
        });

        // 인증 버튼 클릭
        btnVerifyCode.setOnClickListener(v -> {
            String code = editCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "인증번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (code.equals(sentCode)) {
                Toast.makeText(this, "인증되었습니다", Toast.LENGTH_SHORT).show();

                // 새 비밀번호 영역 표시
                findViewById(R.id.labelNewPassword).setVisibility(View.VISIBLE);
                newPasswordSection.setVisibility(View.VISIBLE);
                findViewById(R.id.labelConfirmPassword).setVisibility(View.VISIBLE);
                findViewById(R.id.inputConfirmPasswordLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.btnChangePassword).setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "인증번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            }
        });

        // 비밀번호 변경 버튼 클릭
        btnChangePassword.setOnClickListener(v -> {
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

            // 실제 서버 비밀번호 변경 요청 로직 필요
            // Firebase Firestore에 비밀번호 업데이트
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String email = editEmail.getText().toString().trim();
            db.collection("patients").document(email)
                .update("password", pw)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "비밀번호 변경에 실패했습니다", Toast.LENGTH_SHORT).show();
                });
        });
    }
}
