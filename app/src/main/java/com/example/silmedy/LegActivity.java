package com.example.silmedy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LegActivity extends AppCompatActivity {

    ImageView btnBack;

    View btnThighs, btnCalf, btnShin, btnFoot;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leg);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnThighs = findViewById(R.id.btnThings);
        btnCalf = findViewById(R.id.btnCalf);
        btnShin = findViewById(R.id.btnShin);
        btnFoot = findViewById(R.id.btnFoot);

        btnThighs.setOnClickListener(v -> {
            Toast.makeText(this, "허벅지가 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnCalf.setOnClickListener(v -> {
            Toast.makeText(this, "무릎이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnShin.setOnClickListener(v -> {
            Toast.makeText(this, "정강이가 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnFoot.setOnClickListener(v -> {
            Toast.makeText(this, "발이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(LegActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}