package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.ui.clinic.ClinicHomeActivity;

public class SymptomChoiceActivity extends AppCompatActivity {

    public LinearLayout cardCold, cardIndigestion, cardHeat, cardHeadache, cardDiabetes, cardHighBlood;
    public ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_choice);

        Intent intent = getIntent();
        String username = intent.getStringExtra("userName");

        cardCold = findViewById(R.id.cardCold);
        cardIndigestion = findViewById(R.id.cardIndigestion);
        cardHeat = findViewById(R.id.cardHeat);
        cardHeadache = findViewById(R.id.cardHeadache);
        cardDiabetes = findViewById(R.id.cardDiabetes);
        cardHighBlood = findViewById(R.id.cardHighBlood);
        btnBack = findViewById(R.id.btnBack);

        // 감기
        cardCold.setOnClickListener(v -> moveToDoctorList("감기", username));
        // 소화불량
        cardIndigestion.setOnClickListener(v -> moveToDoctorList("소화불량", username));
        // 열
        cardHeat.setOnClickListener(v -> moveToDoctorList("열", username));
        // 두통
        cardHeadache.setOnClickListener(v -> moveToDoctorList("두통", username));
        // 당뇨
        cardDiabetes.setOnClickListener(v -> moveToDoctorList("당뇨", username));
        // 고혈압
        cardHighBlood.setOnClickListener(v -> moveToDoctorList("고혈압", username));

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(SymptomChoiceActivity.this, ClinicHomeActivity.class);
            backIntent.putExtra("userName", username);
            startActivity(backIntent);
            finish();
        });
    }

    private void moveToDoctorList(String symptom, String username) {
        Intent intent = new Intent(SymptomChoiceActivity.this, DoctorListActivity.class);
        intent.putExtra("symptom", symptom);
        intent.putExtra("userName", username);
        startActivity(intent);
    }
}