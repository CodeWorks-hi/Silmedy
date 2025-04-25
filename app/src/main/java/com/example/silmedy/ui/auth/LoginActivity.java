package com.example.silmedy.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;

    private TextView btnFindId;
    private TextView btnJoin, btnFindPassword;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 위에 주신 XML과 연결

        // 뷰 연결
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnJoin = findViewById(R.id.btnJoin);
        btnFindId = findViewById(R.id.btnFindId);
        btnFindPassword = findViewById(R.id.btnFindPassword);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("SilmedyPrefs", MODE_PRIVATE);

        // 저장된 로그인 정보 자동 입력
        editEmail.setText(prefs.getString("email", ""));
        editPassword.setText(prefs.getString("password", ""));

        // 자동 로그인 시도
        String savedEmail = prefs.getString("email", "");
        String savedPw = prefs.getString("password", "");

        if (!savedEmail.isEmpty() && !savedPw.isEmpty()) {
            autoLogin(savedEmail, savedPw);
        }

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("patients").document(email).get().addOnSuccessListener(doc -> {
                if (doc.exists() && password.equals(doc.getString("password"))) {
                    saveLoginInfo(email, password);
                    goToMain(email);
                } else {
                    Toast.makeText(this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 회원가입 버튼 클릭 → SingupActivity로 이동
        btnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CheckIdentityActivity.class);
            startActivity(intent);
        });


        // 아이디 버튼 클릭 -> FindIdActivity로 이동
        btnFindId.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindIdActivity.class);
            startActivity(intent);
        });



        // 비밀번호 버튼 클릭 -> FindPasswordActivity로 이동
        btnFindPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void autoLogin(String email, String password) {
        db.collection("users").document(email).get().addOnSuccessListener(doc -> {
            if (doc.exists() && password.equals(doc.getString("password"))) {
                goToMain(email);
            }
        });
    }

    private void saveLoginInfo(String email, String password) {
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    private void goToMain(String email) {
        db.collection("patients").document(email).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String username = doc.getString("name"); // Firestore에 저장된 사용자 이름
                Intent intent = new Intent(LoginActivity.this, ClinicHomeActivity.class);
                intent.putExtra("user_name", username);  // 사용자 이름 전달
                startActivity(intent);
                finish();
            }
        });
    }
}