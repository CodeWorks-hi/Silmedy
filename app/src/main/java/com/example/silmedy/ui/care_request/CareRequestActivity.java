package com.example.silmedy.ui.care_request;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CareRequestActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnToday, btnTomorrow, btnReserve;
    private CheckBox checkSignLanguage;
    private TextView doctorName, doctorClinic, doctorTime;
    private ImageView doctorImage;
    private GridLayout timeButtonContainer;

    private String selectedTime = null;
    private String selectedDay = "today";
    private String doctorNameStr, doctorClinicStr, doctorTimeStr;
    private int doctorImageResId;
    private HashMap<String, String> doctorTimeMapFormatted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_care_request);

        btnBack = findViewById(R.id.btnBack);
        btnToday = findViewById(R.id.btnToday);
        btnTomorrow = findViewById(R.id.btnTomorrow);
        btnReserve = findViewById(R.id.btnReserve);
        checkSignLanguage = findViewById(R.id.checkSignLanguage);
        checkInitialSignLanguageSetting();
        doctorName = findViewById(R.id.doctorName);
        doctorClinic = findViewById(R.id.doctorClinic);
        doctorTime = findViewById(R.id.doctorTime);
        doctorImage = findViewById(R.id.doctorImage);
        timeButtonContainer = (GridLayout) findViewById(R.id.time_button_container);

        // 수어 필요 여부 확인 위해 토큰 필요!!!!!
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // 의사 정보 인텐트 처리
        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        ArrayList<String> part = (ArrayList<String>) intent.getSerializableExtra("part");
        ArrayList<String> symptom = (ArrayList<String>) intent.getSerializableExtra("symptom");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(CareRequestActivity.this, DoctorListActivity.class);
            backIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
            backIntent.putExtra("part", getIntent().getSerializableExtra("part"));
            backIntent.putExtra("symptom", getIntent().getSerializableExtra("symptom"));
            backIntent.putExtra("latitude", latitude);
            backIntent.putExtra("longitude", longitude);
            backIntent.putExtra("finish_on_resume", true); // signal to DoctorListActivity to finish on resume
            startActivity(backIntent);
            finish();
        });
        btnReserve.setEnabled(false);

        int license_number = intent.getIntExtra("license_number", 0);
        doctorNameStr = intent.getStringExtra("doctor_name");
        doctorClinicStr = intent.getStringExtra("doctor_clinic");
        Serializable serializedMap = intent.getSerializableExtra("doctor_time");
        if (serializedMap instanceof HashMap) {
            doctorTimeMapFormatted = (HashMap<String, String>) serializedMap;
            doctorTimeStr = buildScheduleTextFromMap(doctorTimeMapFormatted);
        }
        doctorImageResId = intent.getIntExtra("doctor_image", R.drawable.doc);
        String department = intent.getStringExtra("doctor_department");

        doctorName.setText(doctorNameStr);
        doctorClinic.setText(doctorClinicStr);
        String scheduleText = buildScheduleText(doctorTimeStr);
        doctorTime.setText(scheduleText);
        loadTimeSlots("today");
        doctorImage.setImageResource(doctorImageResId);

        btnToday.setSelected(true);
        btnTomorrow.setSelected(false);

        // Set button labels for 오늘/내일 with special handling for Friday/Saturday
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int todayIndex = calendar.get(java.util.Calendar.DAY_OF_WEEK);

        if (todayIndex == java.util.Calendar.FRIDAY) {
            btnToday.setText("오늘 (금)");
            btnTomorrow.setText("월요일");
        } else if (todayIndex == java.util.Calendar.SATURDAY) {
            btnToday.setText("월요일");
            btnTomorrow.setText("화요일");
        } else {
            btnToday.setText("오늘");
            btnTomorrow.setText("내일");
        }

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
                confirmIntent.putExtra("user_name", username);
                confirmIntent.putExtra("part", part);
                confirmIntent.putExtra("symptom", symptom);
                confirmIntent.putExtra("license_number", license_number);
                confirmIntent.putExtra("doctor_name", doctorNameStr);
                confirmIntent.putExtra("doctor_clinic", doctorClinicStr);
                confirmIntent.putExtra("doctor_department", department);
                confirmIntent.putExtra("selected_time", selectedTime);
                confirmIntent.putExtra("selected_day", selectedDay);
                confirmIntent.putExtra("sign_language_requested", signRequested);
                startActivity(confirmIntent);
                finish();
            }
        });
    }

    private void loadTimeSlots(String day) {
        selectedTime = null;
        btnReserve.setEnabled(false);
        timeButtonContainer.removeAllViews();

        String label = getDayLabel(day);
        String timeRange = doctorTimeMapFormatted != null && doctorTimeMapFormatted.containsKey(label)
            ? doctorTimeMapFormatted.get(label)
            : "휴진";
        List<String> timeList = generateTimeSlots(timeRange);

        renderTimeButtons(timeList);
    }

    private void renderTimeButtons(List<String> timeList) {
        timeButtonContainer.setColumnCount(3); // 3 buttons per row
        LayoutInflater inflater = LayoutInflater.from(this);
        timeButtonContainer.removeAllViews();

        for (String time : timeList) {
            View view = inflater.inflate(R.layout.time_button, timeButtonContainer, false);
            Button timeButton = view.findViewById(R.id.timeButton);
            timeButton.setText(time);
            timeButton.setBackgroundResource(R.drawable.time_slot_selector);
            // Set text color to white for all states
            timeButton.setTextColor(getResources().getColor(R.color.white));

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0;
            gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gridParams.setMargins(8, 8, 8, 8);
            view.setLayoutParams(gridParams);

            if (time.equals("휴진")) {
                timeButton.setEnabled(false);
                timeButton.setAlpha(0.5f);
            } else {
                timeButton.setEnabled(true);
                timeButton.setAlpha(1f);
                timeButton.setOnClickListener(v -> {
                    selectedTime = time;
                    btnReserve.setEnabled(true);
                    // Deselect all buttons
                    for (int i = 0; i < timeButtonContainer.getChildCount(); i++) {
                        View child = timeButtonContainer.getChildAt(i);
                        Button otherButton = child.findViewById(R.id.timeButton);
                        if (otherButton != null) {
                            otherButton.setSelected(false); // 이게 핵심
                        }
                    }
                    timeButton.setSelected(true);
                });
            }

            timeButtonContainer.addView(view);
        }
    }

    private String getDayLabel(String dayType) {
        List<String> weekdays = Arrays.asList("월", "화", "수", "목", "금");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIndex = cal.get(java.util.Calendar.DAY_OF_WEEK);

        if (dayType.equals("today")) {
            if (todayIndex == java.util.Calendar.SATURDAY) {
                return "월";
            } else {
                return (todayIndex >= java.util.Calendar.MONDAY && todayIndex <= java.util.Calendar.FRIDAY) ? weekdays.get(todayIndex - 2) : "";
            }
        } else {
            if (todayIndex == java.util.Calendar.FRIDAY || todayIndex == java.util.Calendar.SATURDAY) {
                return (todayIndex == java.util.Calendar.FRIDAY) ? "월" : "화";
            } else {
                return (todayIndex >= java.util.Calendar.MONDAY && todayIndex <= java.util.Calendar.THURSDAY) ? weekdays.get(todayIndex - 1) : "";
            }
        }
    }

    private String buildScheduleText(String timeStr) {
        List<String> weekdays = Arrays.asList("월", "화", "수", "목", "금");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIndex = cal.get(java.util.Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0 || todayIndex >= weekdays.size()) return "진료 없음";

        String today, tomorrow;
        if (todayIndex == 4) { // Friday
            today = weekdays.get(todayIndex);
            tomorrow = "월";
        } else if (todayIndex == 5 || todayIndex == 6) { // Saturday
            today = "월";
            tomorrow = "화";
        } else {
            today = weekdays.get(todayIndex);
            tomorrow = (todayIndex + 1 < weekdays.size()) ? weekdays.get(todayIndex + 1) : weekdays.get(0);
        }

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
        if (timeRange.equals("휴진")) {
            slots.add("휴진");
            return slots;
        }

        String[] parts = timeRange.split("-");
        if (parts.length != 2) return slots;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            java.util.Date start = sdf.parse(parts[0].trim());
            java.util.Date end = sdf.parse(parts[1].trim());

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(start);
            while (cal.getTime().before(end)) {
                String formattedTime = String.format("%02d:%02d",
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE));
                if (!formattedTime.equals("12:00") && !formattedTime.equals("12:30")) {
                    slots.add(formattedTime);
                }
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

        String today, tomorrow;
        if (todayIndex == 4) { // Friday
            today = "월";
            tomorrow = "화";
        } else if (todayIndex == 5) { // Saturday
            today = "월";
            tomorrow = "화";
        } else {
            today = weekdays.get(todayIndex);
            tomorrow = (todayIndex + 1 < weekdays.size()) ? weekdays.get(todayIndex + 1) : weekdays.get(0);
        }

        StringBuilder sb = new StringBuilder();
        if (timeMap.containsKey(today)) {
            sb.append(today).append(" : ").append(timeMap.get(today)).append("\n");
        }
        if (timeMap.containsKey(tomorrow)) {
            sb.append(tomorrow).append(" : ").append(timeMap.get(tomorrow));
        }
        return sb.toString().trim();
    }
    // Check initial sign language setting from server
    private void checkInitialSignLanguageSetting() {
        new Thread(() -> {
            try {
                String accessToken = new TokenManager(getApplicationContext()).getAccessToken();
                URL url = new URL("http://43.201.73.161:5000/request/signcheck");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean isNeeded = jsonResponse.getBoolean("sign_language_needed");

                    runOnUiThread(() -> checkSignLanguage.setChecked(isNeeded));
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}