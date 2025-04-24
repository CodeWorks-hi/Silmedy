package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.adapter.TimeSlotAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
        doctorTimeStr = intent.getStringExtra("doctor_time");
        doctorImageResId = intent.getIntExtra("doctor_image", R.drawable.doc);

        doctorName.setText(doctorNameStr);
        doctorClinic.setText(doctorClinicStr);
        doctorTime.setText(doctorTimeStr);
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
    }

    private void loadTimeSlots(String day) {
        List<String> baseSlots = Arrays.asList(
                "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "12:00", "12:30",
                "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00", "17:30"
        );

        // 점심시간 필터링
        List<String> timeList = new ArrayList<>();
        for (String time : baseSlots) {
            if (!time.startsWith("13")) {
                timeList.add(time);
            }
        }

        TimeSlotAdapter adapter = new TimeSlotAdapter(timeList, selected -> {
            selectedTime = selected;
            btnReserve.setEnabled(true);
        });

        timeSlotRecycler.setAdapter(adapter);
    }
}