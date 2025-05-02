package com.example.silmedy.ui.photo_clinic;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.silmedy.R;
import com.example.silmedy.ui.care_request.DoctorListActivity;
import com.example.silmedy.ai_model.KerasModelPredict;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.user.MedicalHistoryActivity;
import com.example.silmedy.ui.user.MyPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import org.json.JSONObject;

public class DiagnosisResultsActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    ImageView btnBack, imgDiagnosis, imgResult;
    Button btnReservation;
    TextView textPart, textSymptom, editMedical1, editMedical2;
    OkHttpClient client = new OkHttpClient();
    KerasModelPredict predictor;
    ArrayList<String> symptoms = new ArrayList<>();
    String department = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_diagnosis_results);
        predictor = new KerasModelPredict(this);

        btnBack = findViewById(R.id.btnBack);
        btnReservation = findViewById(R.id.btnReservation);
        imgDiagnosis = findViewById(R.id.imgDiagnosis);
        textPart = findViewById(R.id.textPart);
        textSymptom = findViewById(R.id.textSymptom);
        editMedical1 = findViewById(R.id.editMedical1);
        editMedical2 = findViewById(R.id.editMedical2);
        imgResult = findViewById(R.id.imgResult);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        ArrayList<String> part = (ArrayList<String>) intent.getSerializableExtra("part");
        String imagePath = intent.getStringExtra("image_path");

        imgDiagnosis.setImageDrawable(null);  // Clear old image
        Glide.with(this).clear(imgDiagnosis);  // Force Glide to clear old cache
        symptoms.clear();                      // Clear old symptom results

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .skipMemoryCache(true)  // Skip memory cache
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip disk cache
                .into(imgDiagnosis);
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                symptoms.add(predictor.predict(bitmap));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        textPart.setText("부위 : " + part.get(0));

        textSymptom.setText("증상 : " + symptoms.get(0));

        if (symptoms.get(0).equals("정상피부")) {
            editMedical1.setText("정상 피부");
            editMedical2.setText("진료가 필요하지 않습니다.");
            Glide.with(this)
                    .load("https://silmedy-bucket.s3.ap-northeast-2.amazonaws.com/14.+%E1%84%8C%E1%85%A5%E1%86%BC%E1%84%89%E1%85%A1%E1%86%BC+%E1%84%91%E1%85%B5%E1%84%87%E1%85%AE.png")
                    .into(imgResult);
        } else {
            String serverUrl = "http://43.201.73.161:5000/info-by-symptom";

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("symptom", symptoms.get(0));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            RequestBody requestBody = RequestBody.create(
                    jsonBody.toString(),
                    okhttp3.MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        runOnUiThread(() -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                department = jsonResponse.optString("department", "");
                                String subDepartment = jsonResponse.optString("sub_department", "");
                                String imgPath = jsonResponse.optString("image_url", "");
                                editMedical1.setText("추천 진료과 1 : " + department);
                                editMedical2.setText("추천 진료과 2 : " + subDepartment);
                                Glide.with(this)
                                        .load(imgPath)
                                        .into(imgResult);
                            } catch (Exception e) {
                                textPart.append("\n서버 응답 파싱 실패");
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            textPart.append("\n서버 응답 실패");
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        textPart.append("\n서버 연결 실패");
                    });
                }
            }).start();
        }

        btnReservation.setOnClickListener(v -> {
            Intent resultIntent = new Intent(DiagnosisResultsActivity.this, DoctorListActivity.class);
            resultIntent.putExtra("user_name", username);
            resultIntent.putExtra("part", part);
            resultIntent.putStringArrayListExtra("symptom", symptoms);
            resultIntent.putExtra("department", "외과");
            startActivity(resultIntent);
            finish();
        });



        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DiagnosisResultsActivity.this, ShootingActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("part", part);
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