package com.example.silmedy.ui.photo_clinic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;

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
        String email = intent.getStringExtra("email");

        btnBack = findViewById(R.id.btnBack);
        btnThighs = findViewById(R.id.btnThighs);
        btnCalf = findViewById(R.id.btnCalf);
        btnShin = findViewById(R.id.btnShin);
        btnFoot = findViewById(R.id.btnFoot);

        btnThighs.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("email", email);
            shootIntent.putExtra("part", "허벅지");
            startActivity(shootIntent);
            finish();
        });

        btnCalf.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("email", email);
            shootIntent.putExtra("part", "무릎");
            startActivity(shootIntent);
            finish();
        });

        btnShin.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("email", email);
            shootIntent.putExtra("part", "종아리");
            startActivity(shootIntent);
            finish();
        });

        btnFoot.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            shootIntent.putExtra("email", email);
            shootIntent.putExtra("part", "발");
            startActivity(shootIntent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(LegActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("email", email);
            startActivity(backIntent);
            finish();
        });
    }
}