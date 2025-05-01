package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.MedicalHistoryAdapter;
import com.example.silmedy.model.MedicalHistoryItem;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MedicalHistoryActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    ImageView btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_medical_history);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);

        // 뒤로가기 버튼
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
                    Toast.makeText(this, "현재 진료내역 화면입니다.", Toast.LENGTH_SHORT).show();
                    return true;
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

        // --- 진단 내역 리스트 불러오기 ---
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String token = tokenManager.getAccessToken();
        String url = "http://43.201.73.161:5000/diagnosis/list"; // 실제 서버 주소로 변경 필요

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MedicalHistoryActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject jsonObject = new org.json.JSONObject(responseData);
                        org.json.JSONArray recordsArray = jsonObject.getJSONArray("records");
                        java.util.List<MedicalHistoryItem> diagnosisList = new java.util.ArrayList<>();
                        for (int i = 0; i < recordsArray.length(); i++) {
                            org.json.JSONObject item = recordsArray.getJSONObject(i);
                            String diagnosisId = item.getString("diagnosis_id");
                            String diagnosedAt = item.getString("diagnosed_at");
                            String summaryText = item.getString("summary_text");
                            String hospitalName = item.getString("hospital_name");
                            diagnosisList.add(new MedicalHistoryItem(diagnosisId, diagnosedAt, hospitalName, summaryText));
                        }
                        runOnUiThread(() -> {
                            RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
                            MedicalHistoryAdapter adapter = new MedicalHistoryAdapter(diagnosisList);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MedicalHistoryActivity.this));
                        });
                    } catch (org.json.JSONException e) {
                        android.util.Log.e("DIAGNOSIS", "JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MedicalHistoryActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
        // --- 진단 내역 리스트 불러오기 끝 ---
    }
}