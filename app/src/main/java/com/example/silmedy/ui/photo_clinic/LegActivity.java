package com.example.silmedy.ui.photo_clinic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;

public class LegActivity extends AppCompatActivity {

    ImageView btnBack;

    View btnThings, btnCalf, btnShin, btnFoot;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leg);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnThings = findViewById(R.id.btnThings);
        btnCalf = findViewById(R.id.btnCalf);
        btnShin = findViewById(R.id.btnShin);
        btnFoot = findViewById(R.id.btnFoot);

        btnThings.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnCalf.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnShin.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnFoot.setOnClickListener(v -> {
            Intent shootIntent = new Intent(LegActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
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