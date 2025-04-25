package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ArmActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnUpperArm, btnLowerArm, btnHand, btnShoulder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Toast.makeText(this, "팔이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnLowerArm.setOnClickListener(v -> {
            Toast.makeText(this, "팔이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnHand.setOnClickListener(v -> {
            Toast.makeText(this, "손이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnShoulder.setOnClickListener(v -> {
            Toast.makeText(this, "어깨가 선택되었습니다", Toast.LENGTH_SHORT).show();
        });





        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ArmActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}
