package com.example.silmedy.ui.call;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.ApiClient;
import com.example.silmedy.ui.config.ApiService;
import com.example.silmedy.ui.config.TokenManager;


public class IncomingCallActivity extends AppCompatActivity {

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_incoming_call);

    }
}