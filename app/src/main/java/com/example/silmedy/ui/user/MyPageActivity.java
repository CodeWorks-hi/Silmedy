package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.auth.FindPasswordActivity;
import com.example.silmedy.ui.config.TokenManager;

public class MyPageActivity extends AppCompatActivity {


    Button btnChangeProfile,btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_my_page);

        Intent intent = getIntent();
        String username = intent.getStringExtra("user_name");

        btnChangeProfile = findViewById(R.id.btnChangeProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangeProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MyPageActivity.this,MyEditActivity.class);
            profileIntent.putExtra("user_name",username);
            startActivity(profileIntent);
        });


    }
}