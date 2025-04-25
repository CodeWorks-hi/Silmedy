package com.example.silmedy.ui.photo_clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.silmedy.R;

public class ShootingActivity extends AppCompatActivity {

    LinearLayout guideOverlay;
    Button btnHome,btnNext;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        guideOverlay = findViewById(R.id.guideOverlay);
        btnHome = findViewById(R.id.btnHome);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // 액티비티가 시작될 때 안내 메시지 카드 보이기
        guideOverlay.setVisibility(View.VISIBLE);

        // "홈으로" 버튼 클릭 시 홈으로 이동
        btnHome.setOnClickListener(v -> {
            // 홈 화면으로 이동하는 코드 (예: MainActivity로 이동)
            finish(); // 현재 Activity 종료
        });

        // "다음" 버튼 클릭 시 안내 메시지 카드 숨기기
        btnNext.setOnClickListener(v -> {
            // 안내 메시지 숨기기
            guideOverlay.setVisibility(View.GONE);

            // 다른 필요한 동작을 추가 (예: 사진 촬영 화면으로 이동 등)
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ShootingActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });

    }

}
