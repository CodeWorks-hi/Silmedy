package com.example.silmedy.ui.auth;

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

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.ui.open_api.PostalCodeActivity;
import com.example.silmedy.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private static final int POSTCODE_REQUEST_CODE = 1001;

    private EditText editEmail, editPassword, editConfirmPassword, editName, editDetailAddress, editBirthDate;
    private TextView contactView, zipView, addressView;
    private Button btnSignup, btnZipSearch, btnCheckEmail;
    private CheckBox checkboxSignLang;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 뷰 바인딩
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editName = findViewById(R.id.editName);
        contactView = findViewById(R.id.contactView);
        zipView = findViewById(R.id.zipView);
        addressView = findViewById(R.id.addressView);
        editDetailAddress = findViewById(R.id.editDetailAddress);
        editBirthDate = findViewById(R.id.editBirthDate);
        btnSignup = findViewById(R.id.btnSignup);
        btnZipSearch = findViewById(R.id.btnZipSearch);
        btnCheckEmail = findViewById(R.id.btnCheckEmail);
        checkboxSignLang = findViewById(R.id.checkboxSignLang);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("contact")) {
            String receivedContact = intent.getStringExtra("contact");
            contactView.setText(receivedContact);
        }

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        AtomicBoolean isValidEmail = new AtomicBoolean(false);

        // OkHttp 클라이언트 준비
        OkHttpClient client = new OkHttpClient();

        btnCheckEmail.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/patient/check-email";
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
            } catch (Exception e) {
                Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    boolean exists = false;
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        exists = jsonResponse.getBoolean("exists");
                        Log.e("EMAIL EXISTS", String.valueOf(exists));
                    } catch (Exception e) {
                        Log.e("EMAIL EXISTS", "JSON 파싱 오류: " + e.getMessage());
                    }
                    if (exists == false) {
                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                            isValidEmail.set(true);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show();
                            isValidEmail.set(false);
                        });
                    }
                }
            });
        });

        editBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(SignupActivity.this);
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

        // 우편번호 검색
        btnZipSearch.setOnClickListener(v -> {
            Intent newIntent = new Intent(SignupActivity.this, PostalCodeActivity.class);
            startActivityForResult(newIntent, POSTCODE_REQUEST_CODE);
        });

        // 회원가입 버튼 클릭
        btnSignup.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();
            String name = editName.getText().toString().trim();
            String birthDate = editBirthDate.getText().toString().trim();
            String contact = contactView.getText().toString().trim();
            String postalCode = zipView.getText().toString().trim();
            String address = addressView.getText().toString().trim();
            String addressDetail = editDetailAddress.getText().toString().trim();
            boolean isSignLangChecked = checkboxSignLang.isChecked();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
                    || TextUtils.isEmpty(name) || TextUtils.isEmpty(contact)
                    || TextUtils.isEmpty(postalCode) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, getString(R.string.toast_all_fields_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail.get()) {
                Toast.makeText(this, "유효하지 않은 이메일입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.toast_password_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.matches("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{6,}$")) {
                Toast.makeText(this, "비밀번호는 소문자와 숫자를 포함한 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://43.201.73.161:5000/patient/signup";
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
                json.put("password", password);
                json.put("name", name);
                json.put("birth_date", birthDate);
                json.put("contact", contact);
                json.put("postal_code", postalCode);
                json.put("address", address);
                json.put("address_detail", addressDetail);
                json.put("sign_language_needed", String.valueOf(isSignLangChecked));
            } catch (Exception e) {
                Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "회원가입 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, getString(R.string.toast_signup_success), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, getString(R.string.toast_signup_fail) + ": " + resp, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POSTCODE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String zipCode = data.getStringExtra("zipCode");
            String addressResult = data.getStringExtra("address");
            zipView.setText(zipCode);
            addressView.setText(addressResult);
        }
    }
}
