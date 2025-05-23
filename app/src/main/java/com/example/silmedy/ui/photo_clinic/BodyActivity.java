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

public class BodyActivity extends AppCompatActivity {

    ImageView btnBack;
    View btnChest, btnAbdomen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_body);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnChest = findViewById(R.id.btnChest);
        btnAbdomen = findViewById(R.id.btnAbdomen);

        btnChest.setOnClickListener(v -> {
            Intent shootIntent = new Intent(BodyActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("가슴");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnAbdomen.setOnClickListener(v -> {
            Intent shootIntent = new Intent(BodyActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            ArrayList<String> parts = new ArrayList<>();
            parts.add("복부");
            shootIntent.putExtra("part", parts);
            startActivity(shootIntent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(BodyActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });

    }
}