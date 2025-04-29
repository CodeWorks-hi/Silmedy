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
import com.example.silmedy.model.LoginRequest;
import com.example.silmedy.model.LoginResponse;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.ApiClient;
import com.example.silmedy.ui.config.ApiService;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ApiService apiService;

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

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("patients").get().addOnSuccessListener(querySnapshot -> {
                boolean loginSuccess = false;
                String foundDocId = null;
                String foundUsername = null;

                for (var doc : querySnapshot.getDocuments()) {
                    String docEmail = doc.getString("email");
                    String docPassword = doc.getString("password");
                    if (email.equals(docEmail) && password.equals(docPassword)) {
                        loginSuccess = true;
                        foundDocId = doc.getId();
                        foundUsername = doc.getString("name");
                        break;
                    }
                }

                LoginRequest loginRequest = new LoginRequest(email, password);

                if (loginSuccess) {
                    apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String accessToken = response.body().getAccessToken();
                                String refreshToken = response.body().getRefreshToken();

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("access_token", accessToken);
                                editor.putString("refresh_token", refreshToken);
                                editor.apply();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                    saveLoginInfo(email, password);
                    Intent intent = new Intent(LoginActivity.this, ClinicHomeActivity.class);
                    intent.putExtra("user_name", foundUsername);
                    startActivity(intent);
                    finish();
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
        db.collection("patients").whereEqualTo("email", email).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                var doc = querySnapshot.getDocuments().get(0);
                String docPassword = doc.getString("password");
                if (password.equals(docPassword)) {
                    String patientId = doc.getId();
                    goToMain(patientId);
                }
            }
        });
    }

    private void saveLoginInfo(String email, String password) {
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    private void goToMain(String patientId) {
        db.collection("patients").document(patientId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String username = doc.getString("name");
                Intent intent = new Intent(LoginActivity.this, ClinicHomeActivity.class);
                intent.putExtra("user_name", username);
                startActivity(intent);
                finish();
            }
        });
    }
}