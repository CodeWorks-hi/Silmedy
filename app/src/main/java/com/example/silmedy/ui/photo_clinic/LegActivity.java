package com.example.silmedy.ui.photo_clinic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

import java.io.Serializable;
import java.util.ArrayList;

public class LegActivity extends AppCompatActivity {

    ImageView btnBack;

    View btnThighs, btnCalf, btnShin, btnFoot;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_leg);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnThighs = findViewById(R.id.btnThighs);
        btnCalf = findViewById(R.id.btnCalf);
        btnShin = findViewById(R.id.btnShin);
        btnFoot = findViewById(R.id.btnFoot);

        btnThighs.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("허벅지");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnCalf.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("무릎");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnShin.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("종아리");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnFoot.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("발");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(LegActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}