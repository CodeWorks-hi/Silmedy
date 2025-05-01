package com.example.silmedy.ui.prescription;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.user.MedicalHistoryActivity;
import com.example.silmedy.ui.user.MyPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PrescriptionActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;


    ImageView btnVisit, btnDelivery,btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_prescription);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String patient_id = intent.getStringExtra("patient_id");

        btnVisit = findViewById(R.id.btnVisit);
        btnDelivery = findViewById(R.id.btnDelivery);
        btnBack = findViewById(R.id.btnBack);

        // 약국 방문 버튼
        btnVisit.setOnClickListener(v -> {
            Intent visitIntent = new Intent(PrescriptionActivity.this,PharmacyListActivity.class);
            visitIntent.putExtra("user_name",username);
            visitIntent.putExtra("patient_id",patient_id);
            visitIntent.putExtra("serviceType", "방문");
            startActivity(visitIntent);
            finish();
        });

        // 배달 신청 버튼
        btnDelivery.setOnClickListener(v -> {
            Intent deliveryIntent = new Intent(PrescriptionActivity.this,DeliveryInputActivity.class);
            deliveryIntent.putExtra("user_name",username);
            deliveryIntent.putExtra("patient_id",patient_id);
            deliveryIntent.putExtra("serviceType", "배달");
            startActivity(deliveryIntent);
            finish();
        });

        //뒤로가기 -> 클리닉 홈으로
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
}