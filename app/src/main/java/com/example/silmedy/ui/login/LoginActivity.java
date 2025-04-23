package com.example.silmedy.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.login.FindPasswordActivity;
import com.example.silmedy.ui.login.SignupActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText editId, editPassword;
    Button btnLogin;
    TextView btnJoin, btnFindPassword;
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

        // SharedPreferences 초기화
        prefs = getSharedPreferences("SilmedyPrefs", MODE_PRIVATE);

        // 저장된 정보로 자동 로그인
        String savedNick = prefs.getString("nickname", "");
        String savedPw = prefs.getString("password", "");

        if (!savedNick.isEmpty() && !savedPw.isEmpty()) {
            autoLogin(savedNick, savedPw);
        }

        // 로그인 버튼 이벤트
        btnLogin.setOnClickListener(v -> {
            String email = editId.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://pnzx78swvl.execute-api.ap-northeast-2.amazonaws.com/dev/patient/login"; // 수정 필요

            JSONObject loginData = new JSONObject();
            try {
                loginData.put("email", email);
                loginData.put("password", password);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

// 응답 처리 부분 수정
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    loginData,
                    response -> {
                        try {
                            Log.d("LOGIN_RESPONSE", response.toString());

                            int statusCode = response.optInt("statusCode", -1);
                            String bodyString = response.optString("body", "");

                            if (statusCode == -1 || bodyString.isEmpty()) {
                                Toast.makeText(LoginActivity.this, "응답 오류: 필드 누락", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // body는 문자열로 감싸진 JSON이므로 다시 파싱
                            JSONObject body = new JSONObject(bodyString);

                            if (statusCode == 200) {
                                goToMain();
                            } else {
                                String errorMsg = body.optString("error", "로그인 실패");
                                Toast.makeText(LoginActivity.this, "로그인 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "응답 처리 중 오류", Toast.LENGTH_SHORT).show();
                            Log.e("LOGIN_ERROR", "파싱 예외", e);
                        }
                    },
                    error -> {
                        Toast.makeText(LoginActivity.this, "로그인 요청 실패", Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_FAIL", "네트워크 오류", error);
                    }
            );


            Volley.newRequestQueue(this).add(request);
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
        // 기존 Firestore 기반 자동 로그인은 유지하거나, 필요에 따라 AWS Lambda 기반으로 변경하세요.
        // 이 부분은 AWS Lambda 로그인 API로 대체할 수 있습니다.
    }

    private void saveInfo(String nickname, String password) {
        prefs.edit()
                .putString("nickname", nickname)
                .putString("password", password)
                .apply();
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, ClinicHomeActivity.class);
        startActivity(intent);
        finish();
    }
}