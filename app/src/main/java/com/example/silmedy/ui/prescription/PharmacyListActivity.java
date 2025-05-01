package com.example.silmedy.ui.prescription;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

public class PharmacyListActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView btnChangeLocation,locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_pharmacy_list);

        btnBack = findViewById(R.id.btnBack);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        locationText = findViewById(R.id.locationText);

        // 내위 치 설정

        // 약국 리스트 불러오기

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());
    }
}