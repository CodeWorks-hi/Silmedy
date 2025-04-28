package com.example.silmedy.ui.photo_clinic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.silmedy.R;
import com.example.silmedy.ui.care_request.DoctorListActivity;

public class DiagnosisResultsActivity extends AppCompatActivity {

    ImageView btnBack, imgDiagnosis;
    Button btnReservation;
    TextView textPart, textSymptom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_results);

        btnBack = findViewById(R.id.btnBack);
        btnReservation = findViewById(R.id.btnReservation);
        imgDiagnosis = findViewById(R.id.imgDiagnosis);
        textPart = findViewById(R.id.textPart);
        textSymptom = findViewById(R.id.textSymptom);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String patient_id = intent.getStringExtra("patient_id");
        String part = intent.getStringExtra("part");
        String imagePath = intent.getStringExtra("image_path");

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .into(imgDiagnosis);
        }

        textPart.setText("부위 : " + part);



        // AI 진단 프로세스 아래에 작성
        // -->


        // AI 진단 결과 아래에 저장
        // 병명 (결과 리스트를 그대로 저장?)
        // ArrayList<String> symptom = 결과 리스트;

        // 진료 과목 (결과 리스트 중 첫 항목만 저장?)
        // String department = 결과 리스트 중 첫 항목;

        btnReservation.setOnClickListener(v -> {
            Intent resultIntent = new Intent(DiagnosisResultsActivity.this, DoctorListActivity.class);
            resultIntent.putExtra("user_name", username);
            resultIntent.putExtra("patient_id", patient_id);
            resultIntent.putExtra("part", part);
//            resultIntent.putExtra("symptom", symptom);
//            resultIntent.putExtra("department", department);
            startActivity(resultIntent);
            finish();
        });



        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DiagnosisResultsActivity.this, ShootingActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("patient_id", patient_id);
            backIntent.putExtra("part", part);
            startActivity(backIntent);
        });
    }
}