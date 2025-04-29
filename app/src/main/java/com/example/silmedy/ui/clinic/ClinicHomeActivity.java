package com.example.silmedy.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.silmedy.ui.photo_clinic.BodyMain;
import com.example.silmedy.R;
import com.example.silmedy.llama.LlamaActivity;
import com.example.silmedy.ui.care_request.SymptomChoiceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.silmedy.ui.config.TokenManager;

public class ClinicHomeActivity extends AppCompatActivity {

    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private TextView textGreeting;

    private CardView cardAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_clinic_home);

        //  뒤로가기 버튼
        btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // 사용자 이름 환영 메시지 세팅
        Intent intent = getIntent();
        textGreeting = findViewById(R.id.text_greeting);
        String username = intent.getStringExtra("user_name");

        Log.d("ClinicHome", "userName: " + username);
        if (username != null && !username.isEmpty()) {
            textGreeting.setText(String.format("%s님, 환영합니다.", username));
        }

        // 터치로 증상확인 카드 클릭
        CardView cardTouchSymptom = findViewById(R.id.card_touch_symptom);
        if (cardTouchSymptom != null) {
            cardTouchSymptom.setOnClickListener(v -> {
                Intent bodyIntent = new Intent(this, BodyMain.class);
                bodyIntent.putExtra("user_name", username);
                startActivity(bodyIntent);
            });
        }

        // 일상질환 카드 클릭
        CardView cardCold = findViewById(R.id.card_cold);
        if (cardCold != null) {
            cardCold.setOnClickListener(v -> {
                Intent coldIntent = new Intent(this, SymptomChoiceActivity.class);
                coldIntent.putExtra("user_name", username);
                startActivity(coldIntent);
            });
        }

        // AI 증상확인 카드 클릭
        cardAI = findViewById(R.id.card_ai);
        if (cardAI != null) {
            cardAI.setOnClickListener(v -> {
                Log.d("ClinicHomeActivity", "AI 카드 클릭됨");
                Intent aiIntent = new Intent(this, LlamaActivity.class);
                aiIntent.putExtra("user_name", username);
                startActivity(aiIntent);
            });
        }

        // 하단 네비게이션 바 설정
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent navigationIntent = null;

                if (itemId == R.id.nav_home) {
                    Toast.makeText(this, "🏠 현재 홈 화면입니다.", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_history) {
//                    navigationIntent = new Intent(this, HistoryActivity.class); // replace with actual history activity class
                } else if (itemId == R.id.nav_mypage) {
//                    navigationIntent = new Intent(this, MypageActivity.class); // replace with actual mypage activity class
                }

                if (navigationIntent != null) {
                    navigationIntent.putExtra("user_name", username);
                    startActivity(navigationIntent);
                    return true;
                }
                return false;
            });
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }


}
