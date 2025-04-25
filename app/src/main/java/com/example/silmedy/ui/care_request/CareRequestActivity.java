package com.example.silmedy.ui.care_request;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.TimeSlotAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CareRequestActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnToday, btnTomorrow, btnReserve;
    private CheckBox checkSignLanguage;
    private RecyclerView timeSlotRecycler;
    private TextView doctorName, doctorClinic, doctorTime;
    private ImageView doctorImage;

    private String selectedTime = null;
    private String selectedDay = "today";
    private String doctorNameStr, doctorClinicStr, doctorTimeStr;
    private int doctorImageResId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_request);

        btnBack = findViewById(R.id.btnBack);
        btnToday = findViewById(R.id.btnToday);
        btnTomorrow = findViewById(R.id.btnTomorrow);
        btnReserve = findViewById(R.id.btnReserve);
        checkSignLanguage = findViewById(R.id.checkSignLanguage);
        timeSlotRecycler = findViewById(R.id.timeSlotRecycler);
        doctorName = findViewById(R.id.doctorName);
        doctorClinic = findViewById(R.id.doctorClinic);
        doctorTime = findViewById(R.id.doctorTime);
        doctorImage = findViewById(R.id.doctorImage);

        btnBack.setOnClickListener(v -> finish());
        btnReserve.setEnabled(false);
        timeSlotRecycler.setLayoutManager(new GridLayoutManager(this, 3));

        // 의사 정보 인텐트 처리
        Intent intent = getIntent();
        doctorNameStr = intent.getStringExtra("doctor_name");
        doctorClinicStr = intent.getStringExtra("doctor_clinic");
        Serializable serializedMap = intent.getSerializableExtra("doctor_time");
        if (serializedMap instanceof HashMap) {
            HashMap<String, String> doctorTimeMap = (HashMap<String, String>) serializedMap;
            doctorTimeStr = buildScheduleTextFromMap(doctorTimeMap);
        }
        doctorImageResId = intent.getIntExtra("doctor_image", R.drawable.doc);

        doctorName.setText(doctorNameStr);
        doctorClinic.setText(doctorClinicStr);
        String scheduleText = buildScheduleText(doctorTimeStr);
        doctorTime.setText(scheduleText);
        doctorImage.setImageResource(doctorImageResId);

        btnToday.setSelected(true);
        btnTomorrow.setSelected(false);
        loadTimeSlots("today");

        btnToday.setOnClickListener(v -> {
            selectedDay = "today";
            btnToday.setSelected(true);
            btnTomorrow.setSelected(false);
            loadTimeSlots("today");
        });

        btnTomorrow.setOnClickListener(v -> {
            selectedDay = "tomorrow";
            btnToday.setSelected(false);
            btnTomorrow.setSelected(true);
            loadTimeSlots("tomorrow");
        });

        btnReserve.setOnClickListener(v -> {
            if (selectedTime != null) {
                boolean signRequested = checkSignLanguage.isChecked();
                Intent confirmIntent = new Intent(CareRequestActivity.this, CareRequestCompleteActivity.class);
                confirmIntent.putExtra("doctor_name", doctorNameStr);
                confirmIntent.putExtra("doctor_clinic", doctorClinicStr);
                confirmIntent.putExtra("doctor_time", doctorTimeStr);
                confirmIntent.putExtra("selected_time", selectedTime);
                confirmIntent.putExtra("selected_day", selectedDay);
                confirmIntent.putExtra("sign_language_requested", signRequested);
                startActivity(confirmIntent);
                finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(CareRequestActivity.this, DoctorListActivity.class);
            backIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
            startActivity(backIntent);
            finish();
        });
    }

    private void loadTimeSlots(String day) {
        String timeRange = extractTimeForDay(getDayLabel(day), doctorTimeStr);
        List<String> timeList = generateTimeSlots(timeRange);

        TimeSlotAdapter adapter = new TimeSlotAdapter(timeList, selected -> {
            selectedTime = selected;
            btnReserve.setEnabled(true);
        });

        timeSlotRecycler.setAdapter(adapter);
    }

    private String getDayLabel(String dayType) {
        List<String> weekdays = Arrays.asList("월", "화", "수", "목", "금");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIndex = cal.get(java.util.Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0 || todayIndex >= weekdays.size()) return "";

        return (dayType.equals("today")) ? weekdays.get(todayIndex)
                : (todayIndex + 1 < weekdays.size()) ? weekdays.get(todayIndex + 1) : weekdays.get(0);
    }

    private String buildScheduleText(String timeStr) {
        List<String> weekdays = Arrays.asList("월", "화", "수", "목", "금");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIndex = cal.get(java.util.Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0 || todayIndex >= weekdays.size()) return "진료 없음";

        String today = weekdays.get(todayIndex);
        String tomorrow = (todayIndex + 1 < weekdays.size()) ? weekdays.get(todayIndex + 1) : weekdays.get(0);

        StringBuilder sb = new StringBuilder();
        if (timeStr.contains(today)) {
            String todayTime = extractTimeForDay(today, timeStr);
            sb.append(today).append(" : ").append(todayTime).append("\n");
        }
        if (timeStr.contains(tomorrow)) {
            String tomorrowTime = extractTimeForDay(tomorrow, timeStr);
            sb.append(tomorrow).append(" : ").append(tomorrowTime);
        }
        return sb.toString().trim();
    }

    private String extractTimeForDay(String day, String timeStr) {
        for (String part : timeStr.split("\n")) {
            if (part.startsWith(day)) {
                return part.substring(part.indexOf(":") + 1).trim();
            }
        }
        return "휴진";
    }

    private List<String> generateTimeSlots(String timeRange) {
        List<String> slots = new ArrayList<>();
        if (timeRange.equals("휴진")) return slots;

        String[] parts = timeRange.split("-");
        if (parts.length != 2) return slots;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            java.util.Date start = sdf.parse(parts[0].trim());
            java.util.Date end = sdf.parse(parts[1].trim());

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(start);
            while (cal.getTime().before(end)) {
                slots.add(sdf.format(cal.getTime()));
                cal.add(java.util.Calendar.MINUTE, 30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
    }

    private String buildScheduleTextFromMap(HashMap<String, String> timeMap) {
        List<String> weekdays = Arrays.asList("월", "화", "수", "목", "금");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIndex = cal.get(java.util.Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0 || todayIndex >= weekdays.size()) return "진료 없음";

        String today = weekdays.get(todayIndex);
        String tomorrow = (todayIndex + 1 < weekdays.size()) ? weekdays.get(todayIndex + 1) : weekdays.get(0);

        StringBuilder sb = new StringBuilder();
        if (timeMap.containsKey(today)) {
            sb.append(today).append(" : ").append(timeMap.get(today)).append("\n");
        }
        if (timeMap.containsKey(tomorrow)) {
            sb.append(tomorrow).append(" : ").append(timeMap.get(tomorrow));
        }
        return sb.toString().trim();
    }
}