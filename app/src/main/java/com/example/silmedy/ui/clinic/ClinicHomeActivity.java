package com.example.silmedy.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.silmedy.BodyMain;
import com.example.silmedy.R;
import com.example.silmedy.SymptomChoiceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClinicHomeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private View btnProfile;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_home);

        // 🔙 뒤로가기 버튼
        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // 🧍 터치로 증상확인 카드 클릭 및 🤧 일상질환 카드 클릭 리스너를 Java에서 직접 설정합니다.
        CardView cardTouchSymptom = findViewById(R.id.card_touch_symptom);
        if (cardTouchSymptom != null) {
            cardTouchSymptom.setOnClickListener(v -> onTouchSymptomClick(v));
        }

        CardView cardCold = findViewById(R.id.card_cold);
        if (cardCold != null) {
            cardCold.setOnClickListener(v -> onColdClick(v));
        }



        // ⬇ 하단 네비게이션 바 설정
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Toast.makeText(this, " 현재 홈 화면입니다.", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_history) {
                    Toast.makeText(this, " 진료내역 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    Toast.makeText(this, " 마이페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            // 홈을 기본 선택 상태로 설정
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    // 🧍 터치로 증상확인 카드 클릭 -> BodyMain 이동 (XML의 android:onClick 사용)
    public void onTouchSymptomClick(View view) {
        Intent intent = new Intent(this, BodyMain.class);
        startActivity(intent);
    }

    // 🤧 일상질환 카드 클릭 -> SymptomChoiceActivity 이동 (XML의 android:onClick 사용)
    public void onColdClick(View view) {
        Intent intent = new Intent(this, SymptomChoiceActivity.class);
        startActivity(intent);
    }
}