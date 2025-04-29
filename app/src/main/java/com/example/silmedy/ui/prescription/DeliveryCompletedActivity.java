package com.example.silmedy.ui.prescription;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

public class DeliveryCompletedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_delivery_completed);
    }
}