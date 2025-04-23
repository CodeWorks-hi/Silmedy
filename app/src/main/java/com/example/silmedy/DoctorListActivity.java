package com.example.silmedy;

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

import com.example.silmedy.adapter.DoctorAdapter;
import com.example.silmedy.model.Doctor;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        btnBack = findViewById(R.id.btnBack);
        locationText = findViewById(R.id.locationText);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnSort = findViewById(R.id.btnSort);
        btnGenderFilter = findViewById(R.id.btnGenderFilter);
        doctorRecyclerView = findViewById(R.id.doctorRecyclerView);

        btnBack.setOnClickListener(v -> finish());

        // 위치 변경 클릭 시 카카오맵 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorListActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        });

        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorList = new ArrayList<>();
        doctorList.add(new Doctor(R.drawable.doc, "김정훈", "분당구보건소", "진료 가능 (수) 09:00 ~ 18:00"));
        doctorList.add(new Doctor(R.drawable.doc, "박지윤", "수정구보건소", "진료 가능 (금) 13:00 ~ 17:00"));
        doctorList.add(new Doctor(R.drawable.doc, "이상우", "중원구보건소", "진료 가능 (화) 10:00 ~ 16:00"));
        adapter = new DoctorAdapter(doctorList);
        doctorRecyclerView.setAdapter(adapter);

        // 위치 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermissionAndGetCurrentLocation();
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