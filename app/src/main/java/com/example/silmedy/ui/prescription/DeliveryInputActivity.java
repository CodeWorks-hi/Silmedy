package com.example.silmedy.ui.prescription;

import android.os.Bundle;
import android.util.Log;
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

import com.example.silmedy.R;
import com.example.silmedy.ui.config.TokenManager;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.Url;

public class DeliveryInputActivity extends AppCompatActivity {

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


        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();

        // 기본 배송 정보불러오기
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
                            String name = jsonResponse.optString("name", "이름 없음");
                            String contact = jsonResponse.optString("contact"," 전화번호 없음");
                            String address = jsonResponse.optString("address", "주소 정보 없음");
                            String addressDetail = jsonResponse.optString("address_detail", "상세 주소 정보 없음");
                            postalCode = jsonResponse.optInt("postal_code", 0);
                            boolean isDefaultAddress = jsonResponse.optBoolean("is_default_address", false);

                            // UI에 데이터를 설정
                            txtName.setText(name);
                            txtContact.setText(contact);
                            txtAddress.setText(address);
                            txtDetailAddress.setText(addressDetail);

                            // 기본 배송지 체크박스가 true일 경우, EditText에도 값 설정
                            if (isDefaultAddress) {
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

            }
        });



        // 주소 검색


        // 배송 신청 버튼


        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());
    }
}