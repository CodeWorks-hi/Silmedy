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

import com.example.silmedy.ui.clinic.ClinicHomeActivity;

public class BodyActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnChest, btnAbdomen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnChest = findViewById(R.id.btnChest);
        btnAbdomen = findViewById(R.id.btnAbdomen);

        btnChest.setOnClickListener(v -> {
            Toast.makeText(this, "가슴이 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnAbdomen.setOnClickListener(v -> {
            Toast.makeText(this, "배가 선택되었습니다", Toast.LENGTH_SHORT).show();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(BodyActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });

    }
}