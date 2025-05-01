package com.example.silmedy.ui.care_request;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.user.MedicalHistoryActivity;
import com.example.silmedy.ui.user.MyPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class SymptomChoiceActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    public LinearLayout cardCold, cardIndigestion, cardHeat, cardHeadache, cardDiabetes, cardHighBlood;
    public ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_symptom_choice);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        cardCold = findViewById(R.id.cardCold);
        cardIndigestion = findViewById(R.id.cardIndigestion);
        cardHeat = findViewById(R.id.cardHeat);
        cardHeadache = findViewById(R.id.cardHeadache);
        cardDiabetes = findViewById(R.id.cardDiabetes);
        cardHighBlood = findViewById(R.id.cardHighBlood);
        btnBack = findViewById(R.id.btnBack);
        
        ArrayList<String> parts = new ArrayList<>();
        parts.add("전신");

        // 감기
        cardCold.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("감기");
            moveToDoctorList(parts, symptoms, username);
        });
        // 소화불량
        cardIndigestion.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("소화불량");
            moveToDoctorList(parts, symptoms, username);
        });
        // 열
        cardHeat.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("열");
            moveToDoctorList(parts, symptoms, username);
        });
        // 두통
        cardHeadache.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("두통");
            moveToDoctorList(parts, symptoms, username);
        });
        // 당뇨
        cardDiabetes.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("당뇨");
            moveToDoctorList(parts, symptoms, username);
        });
        // 고혈압
        cardHighBlood.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("고혈압");
            moveToDoctorList(parts, symptoms, username);
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(SymptomChoiceActivity.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });

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
                    navigationIntent = new Intent(this, MyPageActivity.class); // replace with actual mypage activity class
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

    private void moveToDoctorList(ArrayList<String> parts, ArrayList<String> symptoms,
                                  String username) {
        Intent intent = new Intent(SymptomChoiceActivity.this, DoctorListActivity.class);
        intent.putExtra("part", parts);
        intent.putExtra("symptom", symptoms);
        intent.putExtra("user_name", username);
        intent.putExtra("department", "내과");
        startActivity(intent);
    }


}