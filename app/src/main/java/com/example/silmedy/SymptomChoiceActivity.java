package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SymptomChoiceActivity extends AppCompatActivity {

    public LinearLayout cardCold, cardIndigestion, cardHeat, cardHeadache, cardDiabetes, cardHighBlood;
    public ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_choice);

        cardCold = findViewById(R.id.cardCold);
        cardIndigestion = findViewById(R.id.cardIndigestion);
        cardHeat = findViewById(R.id.cardHeat);
        cardHeadache = findViewById(R.id.cardHeadache);
        cardDiabetes = findViewById(R.id.cardDiabetes);
        cardHighBlood = findViewById(R.id.cardHighBlood);
        btnBack = findViewById(R.id.btnBack);

        // 감기
        cardCold.setOnClickListener(v -> moveToDoctorList("감기"));
        // 소화불량
        cardIndigestion.setOnClickListener(v -> moveToDoctorList("소화불량"));
        // 열
        cardHeat.setOnClickListener(v -> moveToDoctorList("열"));
        // 두통
        cardHeadache.setOnClickListener(v -> moveToDoctorList("두통"));
        // 당뇨
        cardDiabetes.setOnClickListener(v -> moveToDoctorList("당뇨"));
        // 고혈압
        cardHighBlood.setOnClickListener(v -> moveToDoctorList("고혈압"));

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void moveToDoctorList(String symptom) {
        Intent intent = new Intent(SymptomChoiceActivity.this, DoctorListActivity.class);
        intent.putExtra("symptom", symptom);
        startActivity(intent);
    }
}