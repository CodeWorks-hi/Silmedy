package com.example.silmedy.ui.prescription;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

public class PrescriptionActivity extends AppCompatActivity {

    Intent intent = getIntent();
    String username = intent.getStringExtra("user_name");
    String patient_id = intent.getStringExtra("patient_id");

    ImageView btnVisit, btnDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_prescription);

        btnVisit = findViewById(R.id.btnVisit);
        btnDelivery = findViewById(R.id.btnDelivery);

        // 약국 방문 버튼
        btnVisit.setOnClickListener(v -> {
            Intent visitIntent = new Intent(PrescriptionActivity.this,PharmacyListActivity.class);
            visitIntent.putExtra("user_name",username);
            visitIntent.putExtra("patient_id",patient_id);
            visitIntent.putExtra("serviceType", "방문");
            startActivity(visitIntent);
            finish();
        });

        // 배달 신청 버튼
        btnDelivery.setOnClickListener(v -> {
            Intent deliveryIntent = new Intent(PrescriptionActivity.this,DeliveryInputActivity.class);
            deliveryIntent.putExtra("user_name",username);
            deliveryIntent.putExtra("patient_id",patient_id);
            deliveryIntent.putExtra("serviceType", "배달");
            startActivity(deliveryIntent);
            finish();
        });
    }
}