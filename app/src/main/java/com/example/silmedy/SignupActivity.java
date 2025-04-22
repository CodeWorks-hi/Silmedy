package com.example.silmedy;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class SingupActivity extends AppCompatActivity {

    private ActivitySingupBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // 바인딩된 뷰
        EditText editEmail = binding.editEmail;
        EditText editPassword = binding.editPassword;
        EditText editConfirmPassword = binding.editConfirmPassword;
        EditText editName = binding.editName;
        EditText editPhone = binding.editPhone;
        EditText editZip = binding.editZip;
        EditText editDetailAddress = binding.editDetailAddress;
        Button btnZipSearch = binding.btnZipSearch;
        Button btnSignup = binding.btnSignup;

        // 우편번호 검색 버튼 (임시로 메시지 출력)
        btnZipSearch.setOnClickListener(v ->
                Toast.makeText(this, "우편번호 검색 기능은 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        );

        // 회원가입 버튼
        btnSignup.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String zip = editZip.getText().toString().trim();
            String detailAddress = editDetailAddress.getText().toString().trim();

            // 유효성 검사
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
                    || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)
                    || TextUtils.isEmpty(zip) || TextUtils.isEmpty(detailAddress)) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore 저장
            Map<String, Object> user = new HashMap<>();
            user.put("email", email);
            user.put("password", password);
            user.put("name", name);
            user.put("phone", phone);
            user.put("zip", zip);
            user.put("detailAddress", detailAddress);

            db.collection("users").document(email).set(user)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show();
                        finish(); // 가입 후 종료
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "회원가입 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}