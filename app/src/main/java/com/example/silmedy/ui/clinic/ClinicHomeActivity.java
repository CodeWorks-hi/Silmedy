package com.example.silmedy.ui.clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.adapter.MedicalHistoryAdapter;
import com.example.silmedy.model.MedicalHistoryItem;
import com.example.silmedy.ui.auth.SignupActivity;
import com.example.silmedy.ui.photo_clinic.BodyMain;
import com.example.silmedy.R;
import com.example.silmedy.llama.LlamaActivity;
import com.example.silmedy.ui.care_request.SymptomChoiceActivity;
import com.example.silmedy.ui.prescription.PrescriptionActivity;
import com.example.silmedy.ui.user.MedicalHistoryActivity;
import com.example.silmedy.ui.user.MyPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.silmedy.ui.config.TokenManager;

import org.json.JSONObject;

public class ClinicHomeActivity extends AppCompatActivity {

//    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;
    private TextView textGreeting;

    private CardView cardAI, cardTouchSymptom, cardCold;
    Button btnPrescription;
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_clinic_home);
//
//        //  뒤로가기 버튼
//        btnBack = findViewById(R.id.btnBack);
//        if (btnBack != null) {
//            btnBack.setOnClickListener(v -> onBackPressed());
//        }

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        textGreeting = findViewById(R.id.text_greeting);
        cardTouchSymptom = findViewById(R.id.card_touch_symptom);
        cardCold = findViewById(R.id.card_cold);
        cardAI = findViewById(R.id.card_ai);

        // 사용자 이름 환영 메시지 세팅
        String url = "http://43.201.73.161:5000/patient/name"; // 실제 서버 주소로 변경 필요

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ClinicHomeActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        username = jsonResponse.getString("name");
                    } catch (Exception e) {
                        Log.e("NAME EXISTS", "JSON 파싱 오류: " + e.getMessage());
                    }
                    if (username != null && !username.isEmpty()) {
                        runOnUiThread(() -> {
                            textGreeting.setText(String.format("%s님, 환영합니다.", username));
                        });
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ClinicHomeActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        // 터치로 증상확인 카드 클릭
        if (cardTouchSymptom != null) {
            cardTouchSymptom.setOnClickListener(v -> {
                Intent bodyIntent = new Intent(this, BodyMain.class);
                bodyIntent.putExtra("user_name", username);
                startActivity(bodyIntent);
            });
        }

        // 일상질환 카드 클릭
        if (cardCold != null) {
            cardCold.setOnClickListener(v -> {
                Intent coldIntent = new Intent(this, SymptomChoiceActivity.class);
                coldIntent.putExtra("user_name", username);
                startActivity(coldIntent);
            });
        }

        // AI 증상확인 카드 클릭
        if (cardAI != null) {
            cardAI.setOnClickListener(v -> {
                Log.d("ClinicHomeActivity", "AI 카드 클릭됨");
                Intent aiIntent = new Intent(this, LlamaActivity.class);
                aiIntent.putExtra("user_name", username);
                startActivity(aiIntent);
            });
        }

        // 확인용
        btnPrescription = findViewById(R.id.btnPrescription);
        btnPrescription.setOnClickListener(v -> {
            Intent prescription = new Intent(ClinicHomeActivity.this, PrescriptionActivity.class);
            prescription.putExtra("user_name",username);
            startActivity(prescription);
        });


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
