package com.example.silmedy.ui.user;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.silmedy.R;
import com.example.silmedy.ui.auth.FindIdActivity;
import com.example.silmedy.ui.auth.SignupActivity;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.open_api.PostalCodeActivity;
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

    private static final int POSTCODE_REQUEST_CODE = 1001;
    EditText editName, editContact, editDetailAddress;
    BottomNavigationView bottomNavigation;
    CheckBox checkboxDefault;
    ImageView btnBack;
    TextView txtPostalCode, txtAddress, txtEmail, editBirthDate;
    Button btnChangeEdit, btnZipSearch;
    String url;
    JSONObject json;
    RequestBody body;
    Request request;


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

        editBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MyEditActivity.this);
                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (month < 9) {
                            if (dayOfMonth < 10) {
                                editBirthDate.setText(year + "-0" + (month + 1) + "-0" + dayOfMonth);
                            } else {
                                editBirthDate.setText(year + "-0" + (month + 1) + "-" + dayOfMonth);
                            }
                        } else {
                            if (dayOfMonth < 10) {
                                editBirthDate.setText(year + "-" + (month + 1) + "-0" + dayOfMonth);
                            } else {
                                editBirthDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                            }
                        }
                        return;
                    }
                });
                dialog.show();
            }
        });

        // 정보 불러오기
        url = "http://43.201.73.161:5000/patient/mypage";
        json = new JSONObject();

        OkHttpClient client = new OkHttpClient();
        body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        request = new Request.Builder()
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

        btnZipSearch.setOnClickListener(v -> {
            Intent newIntent = new Intent(MyEditActivity.this, PostalCodeActivity.class);
            startActivityForResult(newIntent, POSTCODE_REQUEST_CODE);
        });

        // 정보 수정 버튼
        btnChangeEdit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String birthDate = editBirthDate.getText().toString().trim();
            String contact = editContact.getText().toString().trim();
            String postalCode = txtPostalCode.getText().toString().trim();
            String address = txtAddress.getText().toString().trim();
            String addressDetail = editDetailAddress.getText().toString().trim();
            boolean isSignLangChecked = checkboxDefault.isChecked();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(birthDate) || TextUtils.isEmpty(contact)
                    || TextUtils.isEmpty(postalCode) || TextUtils.isEmpty(address)
                    || TextUtils.isEmpty(addressDetail)) {
                Toast.makeText(this, getString(R.string.toast_all_fields_required), Toast.LENGTH_SHORT).show();
                return;
            }

            url = "http://43.201.73.161:5000/patient/update";
            JSONObject updatesJson = new JSONObject();
            try {
                updatesJson.put("name", name);
                updatesJson.put("birth_date", birthDate);
                updatesJson.put("contact", contact);
                updatesJson.put("postal_code", postalCode);
                updatesJson.put("address", address);
                updatesJson.put("address_detail", addressDetail);
                updatesJson.put("sign_language_needed", String.valueOf(isSignLangChecked));

                json.put("updates", updatesJson);
            } catch (Exception e) {
                Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject fullJson = new JSONObject();
            try {
                fullJson.put("updates", updatesJson);
            } catch (Exception e) {
                Toast.makeText(this, "JSON wrapping 오류", Toast.LENGTH_SHORT).show();
                return;
            }
            body = RequestBody.create(fullJson.toString(), MediaType.get("application/json"));
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MyEditActivity.this, "정보 수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(MyEditActivity.this, "회원 정보 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(MyEditActivity.this, "회원 정보 수정에 실패하였습니다." + ": " + resp, Toast.LENGTH_SHORT).show();
                            Log.e("회원 정보 수정", "서버 응답 오류: " + response.code() + ", " + resp);
                        }
                    });
                }
            });
        });

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POSTCODE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String zipCode = data.getStringExtra("zipCode");
            String addressResult = data.getStringExtra("address");
            txtPostalCode.setText(zipCode);
            txtAddress.setText(addressResult);
        }
    }
}
