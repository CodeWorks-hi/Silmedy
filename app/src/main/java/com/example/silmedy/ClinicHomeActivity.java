package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ClinicHomeActivity extends AppCompatActivity {

    ImageView btnProfile, btnAlarm;
    View btnAiChat, btnHealth, btnHome, btnHistory, btnMyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_home);

        // 상단 아이콘
        btnProfile = findViewById(R.id.btnProfile);
        btnAlarm = findViewById(R.id.btnAlarm);

        btnProfile.setOnClickListener(v -> {
            // BobyMain으로 이동
            Intent intent = new Intent(ClinicHomeActivity.this, BobyMain.class);
            startActivity(intent);
        });

        btnAlarm.setOnClickListener(v ->
                Toast.makeText(this, "알림 화면 준비 중", Toast.LENGTH_SHORT).show()
        );

        // 하단 네비게이션
        btnAiChat = findViewById(R.id.nav_ai_chat);
        btnHealth = findViewById(R.id.nav_health);
        btnHome = findViewById(R.id.nav_home);
        btnHistory = findViewById(R.id.nav_history);
        btnMyPage = findViewById(R.id.nav_mypage);

        btnAiChat.setOnClickListener(v ->
                Toast.makeText(this, "AI 문진 이동", Toast.LENGTH_SHORT).show()
        );

        btnHealth.setOnClickListener(v ->
                Toast.makeText(this, "건강관리 이동", Toast.LENGTH_SHORT).show()
        );

        btnHome.setOnClickListener(v ->
                Toast.makeText(this, "현재 홈 화면입니다", Toast.LENGTH_SHORT).show()
        );

        btnHistory.setOnClickListener(v ->
                Toast.makeText(this, "진료 내역 이동", Toast.LENGTH_SHORT).show()
        );

        btnMyPage.setOnClickListener(v ->
                Toast.makeText(this, "마이페이지 이동", Toast.LENGTH_SHORT).show()
        );
    }
}
