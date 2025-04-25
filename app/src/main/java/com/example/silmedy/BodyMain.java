package com.example.silmedy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

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

        btnBack = findViewById(R.id.btnBack);
        btnLeftArm = findViewById(R.id.btnLeftArm);
        btnRightArm = findViewById(R.id.btnRightArm);
        btnHead = findViewById(R.id.btnHead);
        btnBody = findViewById(R.id.btnBody);
        btnLeg = findViewById(R.id.btnLeg);

        btnRightArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnLeftArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnHead.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, HeadActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnBody.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, BodyActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnLeg.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, LegActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });


        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(BodyMain.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}
