package com.example.silmedy.ui.prescription;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.silmedy.R;
import com.example.silmedy.ui.auth.SignupActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.open_api.PostalCodeActivity;
import com.example.silmedy.ui.user.MedicalDetailActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.Url;

public class DeliveryInputActivity extends AppCompatActivity {

    private static final int POSTCODE_REQUEST_CODE = 1001;
    ImageView btnBack;
    TextView txtName,txtContact, txtAddress, txtDetailAddress,txtPostalCode,txtShowAddress;
    EditText editName, editContact, editDetailAddress, editRequest;
    Button btnZipSearch, btnSubmit;
    CheckBox checkboxDefault;

    String url;
    JSONObject json;
    RequestBody body;
    Request request;

    // postalCode를 전역 변수로 선언
    int postalCode = 0;

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
        txtPostalCode = findViewById(R.id.txtPostalCode);
        txtShowAddress = findViewById(R.id.txtShowAddress);
        editName = findViewById(R.id.editName);
        editContact = findViewById(R.id.editContact);
        editDetailAddress = findViewById(R.id.editDetailAddress);
        editRequest = findViewById(R.id.editRequest);
        btnZipSearch = findViewById(R.id.btnZipSearch);
        btnSubmit = findViewById(R.id.btnSubmit);
        checkboxDefault = findViewById(R.id.checkboxDefault);

        Intent intent = getIntent();
        int prescriptionId = intent.getIntExtra("prescription_id", 0);
        boolean isDelivery = intent.getBooleanExtra("is_delivery", false);
        Log.e("DELIVERY_INPUT", "prescriptionId: " + prescriptionId);

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // 기본 배송 정보불러오기
        url = "http://43.201.73.161:5000/patient/default-address";
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread( () -> {
                    Toast.makeText(DeliveryInputActivity.this,"서버 연결 실패", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body() != null ? response.body().string() : "";
                    runOnUiThread( () -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);

                            // JSON에서 필요한 데이터 추출
                            String name = jsonResponse.optString("name", "");
                            String contact = jsonResponse.optString("contact","");
                            String address = jsonResponse.optString("address", "");
                            String addressDetail = jsonResponse.optString("address_detail", "");
                            postalCode = jsonResponse.optInt("postal_code", 0);
                            boolean isDefaultAddress = jsonResponse.optBoolean("is_default_address", false);

                            // UI에 데이터를 설정
                            txtName.setText(name);
                            txtContact.setText(contact);
                            txtAddress.setText(address);
                            txtDetailAddress.setText(addressDetail);

                            // 기본 배송지 체크박스가 true일 경우, EditText에도 값 설정
                            if (isDefaultAddress) {
                                checkboxDefault.setChecked(true);
                                editName.setText(name);
                                editContact.setText(contact);
                                txtPostalCode.setText(String.valueOf(postalCode));
                                txtShowAddress.setText(address);
                                editDetailAddress.setText(addressDetail);
                            }
                        } catch (Exception e) {
                            Log.e("JSON_PARSE_ERROR", "주소 데이터 파싱 오류: " + e.getMessage());
                            Toast.makeText(DeliveryInputActivity.this, "주소 데이터 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(DeliveryInputActivity.this,"서버 응답 오류 : "+response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
        // 기본 배송지 체크박스 상태 변화 처리
        checkboxDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 체크박스를 선택하면 배송지 정보를 EditText에 자동으로 입력
                editName.setText(txtName.getText().toString());
                editContact.setText(txtContact.getText().toString());
                txtPostalCode.setText(String.valueOf(postalCode));
                txtShowAddress.setText(txtAddress.getText().toString());
                editDetailAddress.setText(txtDetailAddress.getText().toString());
            } else {
                // 체크박스를 해제하면 EditText를 비움
                editName.setText("");
                editContact.setText("");
                txtPostalCode.setText("");
                txtShowAddress.setText("");
                editDetailAddress.setText("");
            }
        });

        // 주소 검색
        btnZipSearch.setOnClickListener(v -> {
            Intent newIntent = new Intent(DeliveryInputActivity.this, PostalCodeActivity.class);
            startActivityForResult(newIntent, POSTCODE_REQUEST_CODE);
        });

        // 배송 신청 버튼
        btnSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String contact = editContact.getText().toString();
            String postalCodeStr = txtPostalCode.getText().toString();
            String address = txtShowAddress.getText().toString();
            String detailAddress = editDetailAddress.getText().toString();
            String requestMessage = editRequest.getText().toString();

            if (name.isEmpty() || contact.isEmpty() || address.isEmpty() || postalCodeStr.equals("0")) {
                Toast.makeText(DeliveryInputActivity.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 서버에 배송지 정보 전송
            String url = "http://43.201.73.161:5000/delivery/register";

            JSONObject deliveryData = new JSONObject();
            try {
                deliveryData.put("is_delivery", true);
                deliveryData.put("patient_contact", contact);
                // 수정해야 할 부분!!!! -> 어떤 약국 넣어야 함???
                deliveryData.put("pharmacy_id", 1);

                deliveryData.put("prescription_id", prescriptionId);
                deliveryData.put("address", address + " " + detailAddress);
                deliveryData.put("postal_code", postalCodeStr);
                deliveryData.put("delivery_request", requestMessage);
                deliveryData.put("is_default_address", checkboxDefault.isChecked());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("DELIVERY_RESPONSE", "서버 응답: " + deliveryData);

            RequestBody body = RequestBody.create(deliveryData.toString(), MediaType.get("application/json"));

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(DeliveryInputActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String message = jsonResponse.optString("message", "배송 요청이 등록되었습니다.");
                            Log.e("DELIVERY_RESPONSE", "서버 응답: " + message);

                            runOnUiThread(() -> {
                                Toast.makeText(DeliveryInputActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent(DeliveryInputActivity.this, DeliveryCompletedActivity.class);
                                resultIntent.putExtra("username", name);
                                resultIntent.putExtra("contact", contact);
                                resultIntent.putExtra("address", address + " " + detailAddress);
                                resultIntent.putExtra("requestMessage", requestMessage);
                                startActivity(resultIntent);
                                finish(); // 성공 후 현재 액티비티 종료
                            });
                        } catch (Exception e) {
                            Log.e("DELIVERY_RESPONSE", "JSON 파싱 오류: " + e.getMessage());
                            runOnUiThread(() ->
                                Toast.makeText(DeliveryInputActivity.this, "응답 데이터 처리 중 오류", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(DeliveryInputActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        });

        // 뒤로가기
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
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