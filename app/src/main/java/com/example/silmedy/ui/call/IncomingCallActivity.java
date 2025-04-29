package com.example.silmedy.ui.call;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.ApiClient;
import com.example.silmedy.ui.config.ApiService;


public class IncomingCallActivity extends AppCompatActivity {

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        setContentView(R.layout.activity_incoming_call);

    }
}