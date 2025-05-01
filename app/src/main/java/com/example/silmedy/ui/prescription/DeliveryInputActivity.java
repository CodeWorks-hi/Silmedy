package com.example.silmedy.ui.prescription;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

public class DeliveryInputActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtName,txtContact, txtAddress, txtDetailAddress;
    EditText editName, editContact, editAddress, editRequest;
    Button btnZipSearch, btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_delivery_input);

        btnBack = findViewById(R.id.btnBack);
        txtName = findViewById(R.id.txtName);
        txtContact = findViewById(R.id.txtContact);
        txtAddress = findViewById(R.id.txtAddress);
        txtDetailAddress = findViewById(R.id.txtDetailAddress);
        editName = findViewById(R.id.editName);
        editContact = findViewById(R.id.editContact);
        editAddress = findViewById(R.id.editAddress);
        editRequest = findViewById(R.id.editRequest);
        btnZipSearch = findViewById(R.id.btnZipSearch);
        btnSubmit = findViewById(R.id.btnSubmit);

        // 기본 배송 정보불러오기


        // 배송 정보 입력, 주소 검색


        // 배송 신청 버튼


        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());
    }
}