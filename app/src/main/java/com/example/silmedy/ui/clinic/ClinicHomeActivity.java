package com.example.silmedy.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

        // 뒤로가기 버튼 설정
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // 알림 버튼 연결
        btnAlarm = findViewById(R.id.btn_alarm);
        btnAlarm.setOnClickListener(v ->
                Toast.makeText(this, "\uD83D\uDD14 알림 화면 준비 중입니다.", Toast.LENGTH_SHORT).show()
        );

        // 상단 프로필 버튼 (BobyMain으로 이동)
        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ClinicHomeActivity.this, BobyMain.class);
                startActivity(intent);
            });
        }

        // 하단 네비게이션 메뉴 연결
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Toast.makeText(this, "\uD83C\uDFE0 현재 홈 화면입니다.", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.nav_history:
                    Toast.makeText(this, "\uD83D\uDCCB 진료내역 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.nav_mypage:
                    Toast.makeText(this, "\uD83D\uDC64 마이페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }
}
