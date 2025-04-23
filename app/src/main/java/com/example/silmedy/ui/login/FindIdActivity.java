package com.example.silmedy.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;

public class FindIdActivity extends AppCompatActivity {

    private EditText editPhone, editCode;
    private TextView textMyId, textMyIdLabel;
    private Button btnVerify, btnCode, btnFindPassword, btnGoToLogin;
    private ImageButton btnBack;

    private final String sentCode = "1234"; // 테스트용 고정 인증 코드

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
            String phone = editPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "연락처를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            // 실제 서버에서 인증번호 발송 API 호출 필요
            Toast.makeText(this, "인증번호가 발송되었습니다", Toast.LENGTH_SHORT).show();
        });

        // 인증 버튼
        btnCode.setOnClickListener(v -> {
            String code = editCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "인증 코드를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (code.equals(sentCode)) {
                // 임시: 인증 성공 시 사용자 아이디 조회 (전화번호로)
                String userId = getUserIdByPhone(editPhone.getText().toString());
                if (userId != null) {
                    textMyId.setText(maskEmail(userId));
                    textMyId.setVisibility(View.VISIBLE);
                    textMyIdLabel.setVisibility(View.VISIBLE);
                    btnFindPassword.setVisibility(View.VISIBLE);
                    btnGoToLogin.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "등록된 아이디가 없습니다", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "인증 코드가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            }
        });

        // 버튼 이벤트들
        btnFindPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, FindPasswordActivity.class));
        });

        btnGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private String getUserIdByPhone(String phone) {
        // TODO: 서버/DB 조회로 교체 필요
        if (phone.equals("01012341234")) {
            return "code@naver.com";
        }
        return null;
    }

    private String maskEmail(String email) {
        int index = email.indexOf("@");
        if (index > 2) {
            return email.substring(0, 2) + "**" + email.substring(index - 1);
        }
        return email;
    }
}
