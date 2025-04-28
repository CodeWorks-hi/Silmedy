package com.example.silmedy.ui.photo_clinic;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;

public class BodyMain extends AppCompatActivity {

    ImageView btnBack;
    ImageButton btnLeftArm, btnRightArm, btnHead, btnBody, btnLeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_main); // ✔️ 반드시 존재해야 함

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String patient_id = intent.getStringExtra("patient_id");

        btnBack = findViewById(R.id.btnBack);
        btnLeftArm = findViewById(R.id.btnLeftArm);
        btnRightArm = findViewById(R.id.btnRightArm);
        btnHead = findViewById(R.id.btnHead);
        btnBody = findViewById(R.id.btnBody);
        btnLeg = findViewById(R.id.btnLeg);

        btnRightArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            ArmIntent.putExtra("patient_id", patient_id);
            startActivity(ArmIntent);
            finish();
        });

        btnLeftArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            ArmIntent.putExtra("patient_id", patient_id);
            startActivity(ArmIntent);
            finish();
        });

        btnHead.setOnClickListener(v -> {
            Intent HeadIntent = new Intent(BodyMain.this, HeadActivity.class);
            HeadIntent.putExtra("user_name", username);
            HeadIntent.putExtra("patient_id", patient_id);
            startActivity(HeadIntent);
            finish();
        });

        btnBody.setOnClickListener(v -> {
            Intent BodyIntent = new Intent(BodyMain.this, BodyActivity.class);
            BodyIntent.putExtra("user_name", username);
            BodyIntent.putExtra("patient_id", patient_id);
            startActivity(BodyIntent);
            finish();
        });

        btnLeg.setOnClickListener(v -> {
            Intent LegIntent = new Intent(BodyMain.this, LegActivity.class);
            LegIntent.putExtra("user_name", username);
            LegIntent.putExtra("patient_id", patient_id);
            startActivity(LegIntent);
            finish();
        });


        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(BodyMain.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("patient_id", patient_id);
            startActivity(backIntent);
            finish();
        });
    }
}
