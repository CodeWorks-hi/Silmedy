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

public class ArmActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnUpperArm, btnLowerArm, btnHand, btnShoulder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_arm);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        // 이미지 및 버튼 참조
        btnBack = findViewById(R.id.btnBack);
        btnUpperArm = findViewById(R.id.btnUpperArm);
        btnLowerArm = findViewById(R.id.btnLowerArm);
        btnHand = findViewById(R.id.btnHand);
        btnShoulder = findViewById(R.id.btnShoulder);

        // 버튼 클릭 처리
        btnUpperArm.setOnClickListener(v -> {
            Intent shootIntent = new Intent(ArmActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("팔");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnLowerArm.setOnClickListener(v -> {
            Intent shootIntent = new Intent(ArmActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("팔");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnHand.setOnClickListener(v -> {
            Intent shootIntent = new Intent(ArmActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("손");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnShoulder.setOnClickListener(v -> {
            Intent shootIntent = new Intent(ArmActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("어깨");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });


        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ArmActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}
