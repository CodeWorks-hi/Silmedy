package com.example.silmedy.ui.photo_clinic;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;

public class BodyMain extends AppCompatActivity {

    ImageView btnBack;
    ImageButton btnLeftArm, btnRightArm, btnHead, btnBody, btnLeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_body_main); // ✔️ 반드시 존재해야 함

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnBack = findViewById(R.id.btnBack);
        btnLeftArm = findViewById(R.id.btnLeftArm);
        btnRightArm = findViewById(R.id.btnRightArm);
        btnHead = findViewById(R.id.btnHead);
        btnBody = findViewById(R.id.btnBody);
        btnLeg = findViewById(R.id.btnLeg);

        btnRightArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnLeftArm.setOnClickListener(v -> {
            Intent ArmIntent = new Intent(BodyMain.this, ArmActivity.class);
            ArmIntent.putExtra("user_name", username);
            startActivity(ArmIntent);
            finish();
        });

        btnHead.setOnClickListener(v -> {
            Intent HeadIntent = new Intent(BodyMain.this, HeadActivity.class);
            HeadIntent.putExtra("user_name", username);
            startActivity(HeadIntent);
            finish();
        });

        btnBody.setOnClickListener(v -> {
            Intent BodyIntent = new Intent(BodyMain.this, BodyActivity.class);
            BodyIntent.putExtra("user_name", username);
            startActivity(BodyIntent);
            finish();
        });

        btnLeg.setOnClickListener(v -> {
            Intent LegIntent = new Intent(BodyMain.this, LegActivity.class);
            LegIntent.putExtra("user_name", username);
            startActivity(LegIntent);
            finish();
        });


        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(BodyMain.this, ClinicHomeActivity.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });
    }
}
