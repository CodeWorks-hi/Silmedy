package com.example.silmedy.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.silmedy.PostalCodeActivity;
import com.example.silmedy.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private static final int POSTCODE_REQUEST_CODE = 1001;

    private EditText editEmail, editPassword, editConfirmPassword, editName, editPhone, editDetailAddress;
    private TextView zipView, addressView;
    private Button btnSignup, btnZipSearch, btnCheckEmail, btnVerifyPhone;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 뷰 바인딩
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        zipView = findViewById(R.id.zipView);
        addressView = findViewById(R.id.addressView);
        editDetailAddress = findViewById(R.id.editDetailAddress);
        btnSignup = findViewById(R.id.btnSignup);
        btnZipSearch = findViewById(R.id.btnZipSearch);
        btnCheckEmail = findViewById(R.id.btnCheckEmail);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        btnBack = findViewById(R.id.btnBack);

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 이메일 중복확인 (임시 기능)
        btnCheckEmail.setOnClickListener(v ->
                Toast.makeText(this, "중복확인 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        );

        // 연락처 본인확인 (임시 기능)
        btnVerifyPhone.setOnClickListener(v ->
                Toast.makeText(this, "본인확인 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        );

        // 우편번호 검색 (미완성)
        btnZipSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, PostalCodeActivity.class);
            startActivityForResult(intent, POSTCODE_REQUEST_CODE);
        });

        // 회원가입 버튼 클릭
        btnSignup.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String zip = zipView.getText().toString().trim();
            String address = addressView.getText().toString().trim();
            String addressDetail = editDetailAddress.getText().toString().trim();

            Toast.makeText(this, "환자 등록 요청 중...", Toast.LENGTH_SHORT).show();

            // 입력 유효성 검사
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
                    || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)
                    || TextUtils.isEmpty(zip) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, getString(R.string.toast_all_fields_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.toast_password_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }

            // Lambda 연동으로 사용자 정보 저장
            JSONObject userJson = new JSONObject();
            try {
                userJson.put("email", email);
                userJson.put("password", password); // 실제 서비스에서는 암호화 필요
                userJson.put("name", name);
                userJson.put("phone", phone);
                userJson.put("postal_code", zip);
                userJson.put("address", address);
                userJson.put("address_detail", addressDetail);
            } catch (JSONException e) {
                Toast.makeText(this, "JSON 생성 중 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://pnzx78swvl.execute-api.ap-northeast-2.amazonaws.com/dev/patient";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, userJson,
                response -> {
                    Toast.makeText(this, getString(R.string.toast_signup_success), Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, getString(R.string.toast_signup_fail) + ": " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

            queue.add(request);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POSTCODE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String zipCode = data.getStringExtra("zipCode");
            String addressResult = data.getStringExtra("address");
            zipView.setText(zipCode);
            addressView.setText(addressResult);
        }
    }
}