package com.example.silmedy;

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

public class HeadActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnEyes, btnNose, btnMouth, btnNeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnEyes = findViewById(R.id.btnEyes);
        btnNose = findViewById(R.id.btnNose);
        btnMouth = findViewById(R.id.btnMouth);
        btnNeck = findViewById(R.id.btnNeck);

        btnEyes.setOnClickListener(v -> {
            Toast.makeText(this, "눈이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnNose.setOnClickListener(v -> {
            Toast.makeText(this, "코가 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnMouth.setOnClickListener(v -> {
            Toast.makeText(this, "입이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnNeck.setOnClickListener(v -> {
            Toast.makeText(this, "목이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(HeadActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}