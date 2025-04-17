package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // XML 연결 확인

        btnGoToHome = findViewById(R.id.btnGoToHome);

        btnGoToHome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClinicHomeActivity.class);
            startActivity(intent);
        });
    }
}