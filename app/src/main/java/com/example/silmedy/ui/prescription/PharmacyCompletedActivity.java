package com.example.silmedy.ui.prescription;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.user.MedicalHistoryActivity;
import com.example.silmedy.ui.user.MyPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PharmacyCompletedActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    ImageView btnBack;
    TextView txtPharmacyName, txtPharmacyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_pharmacy_completed);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String pharmacyName = intent.getStringExtra("pharmacy_name");
        String pharmacyContact = intent.getStringExtra("pharmacy_contact");
        String pharmacyId = intent.getStringExtra("pharmacy_id");
        String pharmacyAddress = intent.getStringExtra("pharmacy_address");
        String prescriptionId = intent.getStringExtra("prescription_id");

        btnBack = findViewById(R.id.btnBack);
        txtPharmacyName = findViewById(R.id.txtPharmacyName);
        txtPharmacyContact = findViewById(R.id.txtPharmacyContact);

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // API로 신청 내용 저장!!!!
        new Thread(() -> {
            try {
                URL url = new URL("http://43.201.73.161:5000/delivery/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("is_delivery", false);
                jsonInput.put("pharmacy_id", pharmacyId);
                jsonInput.put("prescription_id", prescriptionId);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("CareRequestComplete", "API call failed: " + e.getMessage(), e);
            }
        }).start();

        // 선택한 약국 이름과, 약국 전화번호
        txtPharmacyName.setText("약국명 : " + pharmacyName);
        txtPharmacyContact.setText("전화번호 : " + pharmacyContact);


        // 뒤로가기 -> 클리닉 홈
        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(PharmacyCompletedActivity.this,ClinicHomeActivity.class);
            backIntent.putExtra("user_name",username);
            startActivity(backIntent);
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
}