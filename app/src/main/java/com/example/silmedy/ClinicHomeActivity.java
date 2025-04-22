package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ClinicHomeActivity extends AppCompatActivity {

    // 상단
    ImageView btnProfile, btnAlarm;

    // 하단 네비게이션
    View btnAiChat, btnHealth, btnHome, btnHistory, btnMyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_home);

        // 상단 프로필 & 알림
        btnProfile = findViewById(R.id.btnProfile);
        btnAlarm = findViewById(R.id.btnAlarm);

        btnProfile.setOnClickListener(v -> {
            // 👤 BobyMain 액티비티로 이동
            Intent intent = new Intent(ClinicHomeActivity.this, BobyMain.class);
            startActivity(intent);
        });

        btnAlarm.setOnClickListener(v ->
                Toast.makeText(this, "🔔 알림 화면 준비 중입니다.", Toast.LENGTH_SHORT).show()
        );

        // 하단 네비게이션 버튼 연결
        btnAiChat = findViewById(R.id.nav_ai_chat);
        btnHealth = findViewById(R.id.nav_health);
        btnHome = findViewById(R.id.nav_home);
        btnHistory = findViewById(R.id.nav_history);
        btnMyPage = findViewById(R.id.nav_mypage);

        // 버튼 클릭 리스너 설정
        btnAiChat.setOnClickListener(v ->
                Toast.makeText(this, "🤖 AI 문진으로 이동합니다.", Toast.LENGTH_SHORT).show()
        );

        btnHealth.setOnClickListener(v ->
                Toast.makeText(this, "🩺 건강관리 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
        );

        btnHome.setOnClickListener(v ->
                Toast.makeText(this, "🏠 현재 홈 화면입니다.", Toast.LENGTH_SHORT).show()
        );

        btnHistory.setOnClickListener(v ->
                Toast.makeText(this, "📋 진료내역 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
        );

        btnMyPage.setOnClickListener(v ->
                Toast.makeText(this, "👤 마이페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
        );
    }
}
