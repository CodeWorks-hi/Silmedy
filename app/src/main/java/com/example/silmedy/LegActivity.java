package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LegActivity extends AppCompatActivity {

    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leg);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);



        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(LegActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}