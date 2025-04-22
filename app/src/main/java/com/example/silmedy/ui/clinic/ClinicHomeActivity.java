package com.example.silmedy.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.BobyMain;
import com.example.silmedy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClinicHomeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_home);

        // 🔙 뒤로가기 버튼
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // 👤 프로필 버튼 (BobyMain으로 이동)
// ClinicHomeActivity.java
        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ClinicHomeActivity.this, BobyMain.class);
                startActivity(intent);
            });
        }

        // ⬇ 하단 네비게이션 바 설정
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "🏠 현재 홈 화면입니다.", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_history) {
                Toast.makeText(this, "📋 진료내역 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Toast.makeText(this, "👤 마이페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        // 홈을 기본 선택 상태로 설정 (선택사항)
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}