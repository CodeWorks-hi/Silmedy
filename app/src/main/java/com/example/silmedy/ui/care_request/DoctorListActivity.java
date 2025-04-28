package com.example.silmedy.ui.care_request;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.silmedy.R;
import com.example.silmedy.model.Doctor;
import com.example.silmedy.adapter.DoctorAdapter;
import com.example.silmedy.ui.open_api.MapActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.Serializable;


public class DoctorListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MAP = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;

    private ImageButton btnBack;
    private TextView locationText, btnChangeLocation;
    private Button btnSort, btnGenderFilter;
    private RecyclerView doctorRecyclerView;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        Intent intent = getIntent();
        ArrayList<String> part = (ArrayList<String>) intent.getSerializableExtra("part");
        ArrayList<String> symptom = (ArrayList<String>) intent.getSerializableExtra("symptom");
        String username = intent.getStringExtra("user_name");
        String patient_id = intent.getStringExtra("patient_id");
        String department = intent.getStringExtra("department");

        btnBack = findViewById(R.id.btnBack);
        locationText = findViewById(R.id.locationText);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnSort = findViewById(R.id.btnSort);
        btnGenderFilter = findViewById(R.id.btnGenderFilter);
        doctorRecyclerView = findViewById(R.id.doctorRecyclerView);

        btnBack.setOnClickListener(v -> finish());

        // 위치 변경 클릭 시 카카오맵 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent kakaoIntent = new Intent(DoctorListActivity.this, MapActivity.class);
            startActivityForResult(kakaoIntent, REQUEST_CODE_MAP);
        });

        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorList = new ArrayList<>();
        // 매우 중요!!! 지금은 하드코딩이지만 실제 작동 과정에서는 license_number 및 hospital_id 통해서 정보 불러오고 넘겨주기.
        HashMap<String, String> schedule_kim = new HashMap<>();
        schedule_kim.put("월", "09:00-18:00");
        schedule_kim.put("화", "09:00-18:00");
        schedule_kim.put("수", "09:00-18:00");
        schedule_kim.put("목", "09:00-18:00");
        schedule_kim.put("금", "휴진");
        HashMap<String, String> schedule_park = new HashMap<>();
        schedule_park.put("월", "09:00-18:00");
        schedule_park.put("화", "09:00-18:00");
        schedule_park.put("수", "휴진");
        schedule_park.put("목", "09:00-18:00");
        schedule_park.put("금", "09:00-18:00");
        HashMap<String, String> schedule_lee = new HashMap<>();
        schedule_lee.put("월", "09:00-18:00");
        schedule_lee.put("화", "휴진");
        schedule_lee.put("수", "09:00-18:00");
        schedule_lee.put("목", "09:00-18:00");
        schedule_lee.put("금", "09:00-18:00");
        doctorList.add(new Doctor(123456, R.drawable.doc, "김정훈", "분당구보건소", "내과", schedule_kim));
        doctorList.add(new Doctor(234567, R.drawable.doc, "박지윤", "수정구보건소", "내과", schedule_park));
        doctorList.add(new Doctor(345678, R.drawable.doc, "이상우", "중원구보건소", "내과", schedule_lee));
        adapter = new DoctorAdapter(doctorList, username, patient_id, part, symptom);
        doctorRecyclerView.setAdapter(adapter);

        // 위치 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermissionAndGetCurrentLocation();

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DoctorListActivity.this, SymptomChoiceActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("patient_id", patient_id);
            backIntent.putExtra("part", part);
            backIntent.putExtra("symptom", symptom);
            startActivity(backIntent);
            finish();
        });
    }

    // 위치 권한 확인 및 요청
    private void checkLocationPermissionAndGetCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }
    }

    // 현재 위치 가져오기
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을 경우 처리 생략
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateLocationTextWithAddress(location);
            } else {
                locationText.setText("위치 정보를 가져올 수 없습니다.");
            }
        });
    }

    // 좌표 → 주소로 변환
    private void updateLocationTextWithAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                locationText.setText(address);
            } else {
                locationText.setText("주소를 찾을 수 없습니다.");
            }
        } catch (IOException e) {
            locationText.setText("지오코딩 오류");
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                locationText.setText("위치 권한이 거부되었습니다.");
            }
        }
    }

    // 카카오맵 → 위치 선택 결과 수신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            String selectedAddress = data.getStringExtra("selected_address");
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                locationText.setText(selectedAddress);
            }
        }
    }
}