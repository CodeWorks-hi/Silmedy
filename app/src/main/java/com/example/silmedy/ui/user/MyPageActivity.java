package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.auth.FindPasswordActivity;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyPageActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    ImageButton btnChangeProfile, btnChangePassword;

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
        btnBack = findViewById(R.id.btnBack);

        // 내 정보 수정하기 버튼
        btnChangeProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MyPageActivity.this,MyEditActivity.class);
            profileIntent.putExtra("user_name",username);
            startActivity(profileIntent);
        });

        // 비밀번호 변경 버튼
        btnChangePassword.setOnClickListener((v -> {
            Intent passwordIntent = new Intent(MyPageActivity.this,FindPasswordActivity.class);
            startActivity(passwordIntent);
        }));

        //로그아웃 버튼

        //회원탈퇴버튼


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