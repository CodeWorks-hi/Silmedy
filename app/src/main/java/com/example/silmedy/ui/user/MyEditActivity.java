package com.example.silmedy.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyEditActivity extends AppCompatActivity {

    EditText editName, editBirthDate, editContact, editDetailAddress;
    BottomNavigationView bottomNavigation;
    CheckBox checkboxDefault;
    ImageView btnBack;
    TextView txtPostalCode, txtAddress, txtEmail;
    Button btnChangeEdit, btnZipSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_my_edit);

        btnBack = findViewById(R.id.btnBack);
        btnChangeEdit = findViewById(R.id.btnChangeEdit);
        btnZipSearch = findViewById(R.id.btnZipSearch);
        editName = findViewById(R.id.editName);
        editBirthDate = findViewById(R.id.editBirthDate);
        editContact = findViewById(R.id.editContact);
        editDetailAddress = findViewById(R.id.editDetailAddress);
        txtPostalCode = findViewById(R.id.txtPostalCode);
        txtAddress = findViewById(R.id.txtAddress);
        txtEmail = findViewById(R.id.txtEmail);
        checkboxDefault = findViewById(R.id.checkboxDefault);
        btnZipSearch = findViewById(R.id.btnZipSearch);

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // 정보 불러오기
        String url = "http://43.201.73.161:5000/patient/mypage";
        JSONObject json = new JSONObject();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyEditActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Log.e("PROFILE DATA", responseData);
                        String email = "";
                        String name = "";
                        String birthDate = "";
                        String contact = "";
                        String detailAddress = "";
                        boolean signLanguageNeeded = false;
                        int postalCode = 0;
                        String address = "";
                        String addressDetail = "";
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            email = jsonResponse.getString("email");
                            name = jsonResponse.getString("name");
                            birthDate = jsonResponse.getString("birth_date");
                            contact = jsonResponse.getString("contact");
                            addressDetail = jsonResponse.getString("address_detail");
                            signLanguageNeeded = jsonResponse.getBoolean("sign_language_needed");
                            postalCode = jsonResponse.getInt("postal_code");
                            address = jsonResponse.getString("address");
                        } catch (Exception e) {
                            Log.e("EMAIL EXISTS", "JSON 파싱 오류: " + e.getMessage());
                        }
                        editName.setText(name);
                        editBirthDate.setText(birthDate);
                        editContact.setText(contact);
                        editDetailAddress.setText(addressDetail);
                        txtPostalCode.setText(String.valueOf(postalCode));
                        txtAddress.setText(address);
                        checkboxDefault.setChecked(signLanguageNeeded);
                        txtEmail.setText(email);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MyEditActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

        // 벙보 수정 버튼


        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 하단 네비게이션 바 설정
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Intent navigationIntent = null;

                if (itemId == R.id.nav_home) {
                    navigationIntent = new Intent(this, ClinicHomeActivity.class);
                } else if (itemId == R.id.nav_history) {
                    navigationIntent = new Intent(this, MedicalHistoryActivity.class); // replace with actual history activity class
                } else if (itemId == R.id.nav_mypage) {
                    navigationIntent = new Intent(this, MyPageActivity.class); // replace with actual mypage activity class
                }

                if (navigationIntent != null) {
                    startActivity(navigationIntent);
                    return true;
                }
                return false;
            });
        }
    }
}