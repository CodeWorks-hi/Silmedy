package com.example.silmedy.ui.care_request;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;

import java.util.ArrayList;

public class SymptomChoiceActivity extends AppCompatActivity {

    public LinearLayout cardCold, cardIndigestion, cardHeat, cardHeadache, cardDiabetes, cardHighBlood;
    public ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_choice);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String email = intent.getStringExtra("email");

        cardCold = findViewById(R.id.cardCold);
        cardIndigestion = findViewById(R.id.cardIndigestion);
        cardHeat = findViewById(R.id.cardHeat);
        cardHeadache = findViewById(R.id.cardHeadache);
        cardDiabetes = findViewById(R.id.cardDiabetes);
        cardHighBlood = findViewById(R.id.cardHighBlood);
        btnBack = findViewById(R.id.btnBack);
        
        ArrayList<String> parts = new ArrayList<>();

        // 감기
        cardCold.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("감기");
            moveToDoctorList(parts, symptoms, username, email);
        });
        // 소화불량
        cardIndigestion.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("소화불량");
            moveToDoctorList(parts, symptoms, username, email);
        });
        // 열
        cardHeat.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("열");
            moveToDoctorList(parts, symptoms, username, email);
        });
        // 두통
        cardHeadache.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("두통");
            moveToDoctorList(parts, symptoms, username, email);
        });
        // 당뇨
        cardDiabetes.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("당뇨");
            moveToDoctorList(parts, symptoms, username, email);
        });
        // 고혈압
        cardHighBlood.setOnClickListener(v -> {
            ArrayList<String> symptoms = new ArrayList<>();
            symptoms.add("고혈압");
            moveToDoctorList(parts, symptoms, username, email);
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(SymptomChoiceActivity.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("email", email);
            startActivity(backIntent);
            finish();
        });
    }

    private void moveToDoctorList(ArrayList<String> parts, ArrayList<String> symptoms,
                                  String username, String email) {
        Intent intent = new Intent(SymptomChoiceActivity.this, DoctorListActivity.class);
        intent.putExtra("part", parts);
        intent.putExtra("symptom", symptoms);
        intent.putExtra("user_name", username);
        intent.putExtra("email", email);
        intent.putExtra("department", "내과");
        startActivity(intent);
    }
}