package com.example.silmedy.ui.care_request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class CareRequestCompleteActivity extends AppCompatActivity {

    TextView editDoctor, editGroup, editDate;
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
        setContentView(R.layout.activity_care_request_complete);

        editDoctor = findViewById(R.id.editDoctor);
        editGroup = findViewById(R.id.editGroup);
        editDate = findViewById(R.id.editDate);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        ArrayList<String> part = (ArrayList<String>) intent.getSerializableExtra("part");
        ArrayList<String> symptom = (ArrayList<String>) intent.getSerializableExtra("symptom");
        int licenseNumber = intent.getIntExtra("license_number", 0);
        String doctorName = intent.getStringExtra("doctor_name");
        String doctorClinic = intent.getStringExtra("doctor_clinic");
        String doctorDepartment = intent.getStringExtra("doctor_department");
        String selectedTime = intent.getStringExtra("selected_time");
        String selectedDay = intent.getStringExtra("selected_day");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final String bookDate;
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if ("today".equals(selectedDay)) {
            if (todayDayOfWeek == Calendar.SATURDAY || todayDayOfWeek == Calendar.SUNDAY) {
                // 오늘이 토요일/일요일이면 월요일로 이동
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
            bookDate = dateFormat.format(calendar.getTime());
        } else if ("tomorrow".equals(selectedDay)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            int tomorrowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (tomorrowDayOfWeek == Calendar.SATURDAY || tomorrowDayOfWeek == Calendar.SUNDAY) {
                // 내일이 토/일이면 화요일로 이동
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            } else if (tomorrowDayOfWeek == Calendar.FRIDAY) {
                // 내일이 금요일이면 월요일로 이동
                calendar.add(Calendar.DAY_OF_YEAR, 3);
            }
            bookDate = dateFormat.format(calendar.getTime());
        } else {
            bookDate = selectedDay;
        }
        boolean signLanguageRequested = intent.getBooleanExtra("sign_language_requested", false);

        // 토큰 가져오기!!!
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        Log.d("CareRequestComplete", "AccessToken: " + accessToken);

        // API로 신청 내용 저장!!!!
        new Thread(() -> {
            try {
                URL url = new URL("http://43.201.73.161:5000/request/confirmed");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                Log.d("CareRequestComplete", "Authorization Header: Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // 임시 하드 코딩
                symptom.add("감기");
                part.add("전신");

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("doctor_id", licenseNumber);
                jsonInput.put("department", doctorDepartment);
                jsonInput.put("symptom_part", new JSONArray(part));
                jsonInput.put("symptom_type", new JSONArray(symptom));
                jsonInput.put("book_date", bookDate);
                jsonInput.put("book_hour", selectedTime);
                jsonInput.put("sign_language_needed", signLanguageRequested);
                Log.d("CareRequestComplete", "Sending request to /request/confirmed with payload: " + jsonInput.toString());

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d("CareRequestComplete", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.d("CareRequestComplete", "Response: " + response.toString());
                } else {
                    Log.e("CareRequestComplete", "Error Response Code: " + responseCode);
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    Log.e("CareRequestComplete", "Error Response Body: " + errorResponse.toString());
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("CareRequestComplete", "API call failed: " + e.getMessage(), e);
            }
        }).start();

        // 화면 표출 데이터 변환
        editDoctor.setText("의사 : " + doctorName);
        editGroup.setText("소속 : " + doctorClinic);

        String displayDay = selectedDay;
        if (displayDay.equals("today")) {
            Calendar calendarNew = Calendar.getInstance();
            int dayOfWeek = calendarNew.get(Calendar.DAY_OF_WEEK);
            displayDay = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) ? "월요일" : "오늘";
        } else if (displayDay.equals("tomorrow")) {
            Calendar calendarNew = Calendar.getInstance();
            calendarNew.add(Calendar.DAY_OF_YEAR, 1);
            int dayOfWeek = calendarNew.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                displayDay = "화요일";
            } else if (dayOfWeek == Calendar.FRIDAY) {
                displayDay = "월요일";
            } else {
                displayDay = "내일";
            }
        }

        editDate.setText("날짜 : " + displayDay + " | " + selectedTime + " ~ "
                + selectedTime.substring(0, 3)
                + (Integer.parseInt(selectedTime.substring(3)) + 20));

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(CareRequestCompleteActivity.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", userName);
            startActivity(backIntent);
            finish();
        });
    }
}