package com.example.silmedy.ui.photo_clinic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;

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
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnNose.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnMouth.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnNeck.setOnClickListener(v -> {
            Intent shootIntent = new Intent(HeadActivity.this, ShootingActivity.class);
            shootIntent.putExtra("user_name", username);
            startActivity(shootIntent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(HeadActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}