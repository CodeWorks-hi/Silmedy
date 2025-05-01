package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.auth.FindPasswordActivity;
import com.example.silmedy.ui.auth.LoginActivity;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyPageActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    ImageButton btnChangeProfile, btnChangePassword, btnLogout, btnSecession;

    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_my_page);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnChangeProfile = findViewById(R.id.btnChangeProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnSecession = findViewById(R.id.btnSecession);
        btnBack = findViewById(R.id.btnBack);

        // 내 정보 수정하기 버튼
        btnChangeProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MyPageActivity.this, MyEditActivity.class);
            startActivity(profileIntent);
        });

        // 비밀번호 변경 버튼
        btnChangePassword.setOnClickListener((v -> {
            Intent passwordIntent = new Intent(MyPageActivity.this, ChangePasswordActivity.class);
            startActivity(passwordIntent);
        }));

        //로그아웃 버튼
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(MyPageActivity.this)
                .setTitle("로그아웃")
                .setMessage("로그아웃하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    Intent logoutIntent = new Intent(MyPageActivity.this, LoginActivity.class);
                    startActivity(logoutIntent);
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
        });

        //회원탈퇴버튼
        btnSecession.setOnClickListener(v -> {
            new AlertDialog.Builder(MyPageActivity.this)
                .setTitle("회원 탈퇴")
                .setMessage("정말로 탈퇴하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    TokenManager tokenManager = new TokenManager(getApplicationContext());
                    String accessToken = tokenManager.getAccessToken();

                    String url = "http://43.201.73.161:5000/patient/delete";
                    JSONObject json = new JSONObject();

                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .delete()
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() -> Toast.makeText(MyPageActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MyPageActivity.this, "회원 탈퇴가 정상적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent logoutIntent = new Intent(MyPageActivity.this, LoginActivity.class);
                                    startActivity(logoutIntent);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(MyPageActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 하단 네비게이션 바 설정
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent navigationIntent = null;

                if (itemId == R.id.nav_home) {
                    navigationIntent = new Intent(this, ClinicHomeActivity.class);
                } else if (itemId == R.id.nav_history) {
                    navigationIntent = new Intent(this, MedicalHistoryActivity.class); // replace with actual history activity class
                } else if (itemId == R.id.nav_mypage) {
                    Toast.makeText(this, "현재 마이페이지 화면입니다.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (navigationIntent != null) {
                    navigationIntent.putExtra("user_name", username);
                    startActivity(navigationIntent);
                    return true;
                }
                return false;
            });
        }


    }
}