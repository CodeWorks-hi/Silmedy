package com.example.silmedy.ui.login;

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
import com.example.silmedy.ui.login.FindPasswordActivity;
import com.example.silmedy.ui.login.SignupActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText editId, editPassword;
    Button btnLogin;
    TextView btnJoin, btnFindPassword;
    FirebaseFirestore db;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 뷰 연결
        editId = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnJoin = findViewById(R.id.btnJoin);
        btnFindPassword = findViewById(R.id.btnFindPassword);

        // 파이어스토어 및 SharedPreferences 초기화
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("SilmedyPrefs", MODE_PRIVATE);

        // 저장된 정보로 자동 로그인
        String savedNick = prefs.getString("nickname", "");
        String savedPw = prefs.getString("password", "");

        if (!savedNick.isEmpty() && !savedPw.isEmpty()) {
            autoLogin(savedNick, savedPw);
        }

        // 로그인 버튼 이벤트
        btnLogin.setOnClickListener(v -> {
            String nickname = editId.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String userDoc = nickname + "_room";
            db.collection("rooms").document(userDoc).get().addOnSuccessListener(doc -> {
                if (doc.exists() && password.equals(doc.getString("password"))) {
                    saveInfo(nickname, password);
                    goToMain(nickname);
                } else {
                    Toast.makeText(this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 회원가입 버튼
        btnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // 비밀번호 찾기 버튼
        btnFindPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void autoLogin(String nickname, String password) {
        String userDoc = nickname + "_room";
        db.collection("rooms").document(userDoc).get().addOnSuccessListener(doc -> {
            if (doc.exists() && password.equals(doc.getString("password"))) {
                goToMain(nickname);
            }
        });
    }

    private void saveInfo(String nickname, String password) {
        prefs.edit()
                .putString("nickname", nickname)
                .putString("password", password)
                .apply();
    }

    private void goToMain(String nickname) {
        Intent intent = new Intent(LoginActivity.this, ClinicHomeActivity.class);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
        finish();
    }
}