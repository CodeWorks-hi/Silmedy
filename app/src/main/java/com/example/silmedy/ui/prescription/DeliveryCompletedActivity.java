package com.example.silmedy.ui.prescription;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

public class DeliveryCompletedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_delivery_completed);
    }
}