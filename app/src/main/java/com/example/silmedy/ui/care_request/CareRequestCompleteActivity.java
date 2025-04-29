package com.example.silmedy.ui.care_request;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.MainActivity;
import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;

public class CareRequestCompleteActivity extends AppCompatActivity {

    TextView editDoctor, editGroup, editDate;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_care_request_complete);

        editDoctor = findViewById(R.id.editDoctor);
        editGroup = findViewById(R.id.editGroup);
        editDate = findViewById(R.id.editDate);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        String part = intent.getStringExtra("part");
        String symptom = intent.getStringExtra("symptom");
        String licenseNumber = intent.getStringExtra("license_number");
        String doctorName = intent.getStringExtra("doctor_name");
        String doctorClinic = intent.getStringExtra("doctor_clinic");
        String doctorDepartment = intent.getStringExtra("doctor_department");
        String selectedTime = intent.getStringExtra("selected_time");
        String selectedDay = intent.getStringExtra("selected_day");
        boolean signLanguageRequested = intent.getBooleanExtra("sign_language_requested", false);

        // 토큰 가져오기!!!
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // API로 신청 내용 저장!!!!
        // -->

        // 화면 표출 데이터 변환
        editDoctor.setText("의사 : " + doctorName);
        editGroup.setText("소속 : " + doctorClinic);

        if (selectedDay.equals("today")) {
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                selectedDay = "월요일";
            } else {
                selectedDay = "오늘";
            }
        } else if (selectedDay.equals("tomorrow")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                selectedDay = "화요일";
            } else if (dayOfWeek == Calendar.FRIDAY) {
                selectedDay = "월요일";
            } else {
                selectedDay = "내일";
            }
        }
        editDate.setText("날짜 : " + selectedDay + " | " + selectedTime + " ~ "
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