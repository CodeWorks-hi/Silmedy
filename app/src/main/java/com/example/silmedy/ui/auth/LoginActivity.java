package com.example.silmedy.ui.auth;

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
import com.example.silmedy.model.LoginRequest;
import com.example.silmedy.model.LoginResponse;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.ApiClient;
import com.example.silmedy.ui.config.ApiService;
import com.example.silmedy.videocall.VideoCallActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS = "SilmedyPrefs";
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

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token != null) {
            // 이미 로그인됨 → 딜레이 없이 Home 또는 Receive로 이동
            String afterRoom = getIntent().getStringExtra("after_login_roomId");
            if (afterRoom != null) {
                startActivity(new Intent(this, VideoCallActivity.class)
                        .putExtra("roomId", afterRoom));
            } else {
                startActivity(new Intent(this, ClinicHomeActivity.class));
            }
            finish();
            return;
        }

        // 뷰 연결
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnJoin = findViewById(R.id.btnJoin);
        btnFindId = findViewById(R.id.btnFindId);
        btnFindPassword = findViewById(R.id.btnFindPassword);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("SilmedyPrefs", MODE_PRIVATE);

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(email, password);

            apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String accessToken = response.body().getAccessToken();
                        String refreshToken = response.body().getRefreshToken();
                        String username = response.body().getUserName();
                        String fcmToken = response.body().getFcmToken();

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("access_token", accessToken);
                        editor.putString("refresh_token", refreshToken);
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.putString("fcm_token", fcmToken);
                        editor.apply();

                        Log.d("LoginActivity", "Saved access token: " + accessToken);
                        Log.d("LoginActivity", "Saved refresh token: " + refreshToken);
                        Log.d("LoginActivity", "Saved fcm token: " + fcmToken);

                        prefs.edit()
                                .putString("access_token", accessToken)
                                .putString("refresh_token", refreshToken)
                                .putString("user_name", username)
                                .putString("fcm_token", fcmToken)
                                .apply();

                        if (fcmToken == null || fcmToken.isEmpty() || fcmToken.equals("null")) {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.e("LoginActivity", "🔥 FCM 토큰 발급: " + task.getResult());
                                            postFcmToken(email, accessToken, task.getResult());
                                        } else {
                                            Log.e("LoginActivity", "🔥 FCM 토큰 발급 실패", task.getException());
                                        }
                                    });
                        } else {
                            Log.e("LoginActivity", "기존 FCM 토큰 사용: " + fcmToken);
                        }

                        runOnUiThread(() -> {
                            String afterRoom = getIntent().getStringExtra("after_login_roomId");
                            if (afterRoom != null) {
                                startActivity(new Intent(LoginActivity.this, VideoCallActivity.class)
                                        .putExtra("roomId", afterRoom));
                            } else {
                                startActivity(new Intent(LoginActivity.this, ClinicHomeActivity.class));
                            }
                            finish();
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(LoginActivity.this, "로그인 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void postFcmToken(String email, String jwt, String fcmToken) {
        Log.d("LoginActivity", "📦 postFcmToken 호출됨. email=" + email + ", jwt=" + jwt + ", fcmToken=" + fcmToken);

        if (jwt == null || jwt.trim().isEmpty() || jwt.equals("null")) {
            Log.e("LoginActivity", "❌ JWT 토큰이 유효하지 않음. FCM 등록 요청 중단");
            return;
        }

        JSONObject b = new JSONObject();
        try {
            b.put("fcm_token", fcmToken);
        } catch (JSONException ignored) {}

        Request.Builder builder = new Request.Builder()
                .url("http://43.201.73.161:5000/patient/fcm-token")
                .post(RequestBody.create(
                        b.toString(),
                        MediaType.get("application/json; charset=utf-8")
                ));

        // Add Authorization header only if the token is valid
        if (jwt != null && !jwt.trim().isEmpty() && !jwt.equals("null")) {
            // Sanitize JWT more robustly
            String cleanedJwt = jwt.replaceAll("[\\n\\r\\s]", "");
            builder.addHeader("Authorization", "Bearer " + cleanedJwt);
            Log.d("LoginActivity", "✅ Authorization 헤더 값: Bearer " + cleanedJwt);
        } else {
            Log.w("LoginActivity", "⚠️ Authorization 헤더가 추가되지 않음: JWT 없음 또는 무효");
        }

        Request r = builder.build();

        Log.d("LoginActivity", "📤 요청 보낼 준비 완료. URL: " + r.url());
        Log.d("LoginActivity", "📤 요청 바디: " + b.toString());
        Log.d("LoginActivity", "📤 요청 헤더: " + r.headers().toString());

        new OkHttpClient().newCall(r).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("LoginActivity", "FCM 등록 실패", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                try {
                    Log.i("LoginActivity", "📥 FCM 등록 응답 바디: " + response.body().string());
                } catch (IOException e) {
                    Log.e("LoginActivity", "FCM 등록 응답 바디 읽기 실패", e);
                }
                Log.i("LoginActivity", "FCM 등록 결과: " + response.code());
            }
        });
    }
}