package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000L; // 스플래시 딜레이 시간 (2초)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 로고가 있는 XML

        // 2초 후 LoginActivity로 자동 이동
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // MainActivity 종료 (뒤로가기 방지)
        }, SPLASH_DELAY);
    }
}