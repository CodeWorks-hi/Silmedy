package com.example.silmedy.ui.photo_clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

import java.util.ArrayList;

public class HeadActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnEyes, btnNose, btnMouth, btnNeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_head);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");
        String patient_id = intent.getStringExtra("patient_id");

        btnBack = findViewById(R.id.btnBack);
        btnEyes = findViewById(R.id.btnEyes);
        btnNose = findViewById(R.id.btnNose);
        btnMouth = findViewById(R.id.btnMouth);
        btnNeck = findViewById(R.id.btnNeck);

        btnEyes.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("patient_id", patient_id);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("눈");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnNose.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("patient_id", patient_id);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("코");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnMouth.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("patient_id", patient_id);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("입");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnNeck.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("patient_id", patient_id);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("목");
            shootIntent.putExtra("part", parts);startActivity(shootIntent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(HeadActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("patient_id", patient_id);
            startActivity(backIntent);
            finish();
        });
    }
}