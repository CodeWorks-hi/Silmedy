package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.prescription.DeliveryInputActivity;
import com.example.silmedy.ui.prescription.PharmacyListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

public class MedicalDetailActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    ImageView btnBack, imgPrescription, btnVisit, btnDelivery;
    String prescriptionUrl = "";
    boolean isMade = false;
    int prescriptionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_medical_detail);

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String hospitalName = intent.getStringExtra("hospital_name");
        String diagnosisId = intent.getStringExtra("diagnosis_id");

        btnBack = findViewById(R.id.btnBack);
        imgPrescription = findViewById(R.id.imgPrescription);
        btnVisit = findViewById(R.id.btnVisit);
        btnDelivery = findViewById(R.id.btnDelivery);


        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(MedicalDetailActivity.this, MedicalHistoryActivity.class);
            backIntent.putExtra("date", date);
            backIntent.putExtra("hospital_name", hospitalName);
            backIntent.putExtra("diagnosis_id", diagnosisId);
            startActivity(backIntent);
            finish();
        });

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String token = tokenManager.getAccessToken();
        String url = "http://43.201.73.161:5000/prescription/url"; // 실제 서버 주소로 변경 필요

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url + "?diagnosis_id=" + diagnosisId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MedicalDetailActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        prescriptionUrl = jsonResponse.getString("prescription_url");
                        isMade = jsonResponse.getBoolean("is_made");
                        prescriptionId = jsonResponse.getInt("prescription_id");

                        Log.d("PRESCRIPTION EXISTS", "처방 ID: " + prescriptionId);
                    } catch (Exception e) {
                        Log.e("PRESCRIPTION EXISTS", "JSON 파싱 오류: " + e.getMessage());
                    }
                    runOnUiThread(() -> {
                        Glide.with(MedicalDetailActivity.this)
                                .load(prescriptionUrl)
                                .into(imgPrescription);
                        if (isMade) {
                            btnVisit.setVisibility(View.GONE);
                            btnDelivery.setVisibility(View.GONE);
                        } else {
                            btnVisit.setVisibility(View.VISIBLE);
                            btnDelivery.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MedicalDetailActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        btnVisit.setOnClickListener(v -> {
            Intent visitIntent = new Intent(MedicalDetailActivity.this, PharmacyListActivity.class);
            visitIntent.putExtra("diagnosis_id", diagnosisId);
            visitIntent.putExtra("date", date);
            visitIntent.putExtra("hospital_name", hospitalName);
            visitIntent.putExtra("prescription_id", prescriptionId);
            startActivity(visitIntent);
            finish();
        });

        btnDelivery.setOnClickListener(v -> {
            Intent deliveryIntent = new Intent(MedicalDetailActivity.this, DeliveryInputActivity.class);
            deliveryIntent.putExtra("diagnosis_id", diagnosisId);
            deliveryIntent.putExtra("date", date);
            deliveryIntent.putExtra("hospital_name", hospitalName);
            deliveryIntent.putExtra("prescription_id", prescriptionId);
            startActivity(deliveryIntent);
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
                    startActivity(navigationIntent);
                    return true;
                }
                return false;
            });
        }
    }
}