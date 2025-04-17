package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnGoToHome;
    private ImageView iconPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGoToHome = findViewById(R.id.btnGoToHome);
        iconPerson = findViewById(R.id.iconPerson);

        btnGoToHome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClinicHomeActivity.class);
            startActivity(intent);
        });

        iconPerson.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BobyMain.class);
            startActivity(intent);
        });
    }
}
