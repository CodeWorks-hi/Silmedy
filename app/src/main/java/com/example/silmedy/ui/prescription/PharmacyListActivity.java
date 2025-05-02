package com.example.silmedy.ui.prescription;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.adapter.PharmacyAdapter;
import com.example.silmedy.model.Pharmacy;
import com.example.silmedy.ui.care_request.DoctorListActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.open_api.MapActivity;
import com.example.silmedy.ui.user.MedicalDetailActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PharmacyListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MAP = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;
    public static double staticLatitude = 0;
    public static double staticLongitude = 0;
    private RecyclerView pharmacyRecyclerView;
    private PharmacyAdapter adapter;
    private List<Pharmacy> pharmacyList;
    ImageView btnBack;
    TextView btnChangeLocation,locationText;
    private FusedLocationProviderClient fusedLocationClient;
    double latitude = 0;
    double longitude = 0;
    private boolean isLocationFromMap = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });
        setContentView(R.layout.activity_pharmacy_list);

        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude", 0);
            longitude = savedInstanceState.getDouble("longitude", 0);
            isLocationFromMap = savedInstanceState.getBoolean("isLocationFromMap", false);
        } else {
            latitude = staticLatitude;
            longitude = staticLongitude;
        }

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String prescriptionId = intent.getStringExtra("prescription_id");
        String diagnosisId = intent.getStringExtra("diagnosis_id");

        btnBack = findViewById(R.id.btnBack);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        locationText = findViewById(R.id.locationText);
        pharmacyRecyclerView = findViewById(R.id.pharmacyRecyclerView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 클라이언트 초기화 및 위치 가져오기
        if (!isLocationFromMap && latitude == 0 && longitude == 0) {
            checkLocationPermissionAndGetCurrentLocation();
            // fetchDoctors will be called after location is fetched in getCurrentLocation()
        } else {
            // Use restored or map location
            fetchPharmacies(latitude, longitude);
        }

        // 위치 변경 클릭 시 카카오맵 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent kakaoIntent = new Intent(PharmacyListActivity.this, MapActivity.class);
            startActivityForResult(kakaoIntent, REQUEST_CODE_MAP);
        });

        // 약국 리스트 불러오기
        pharmacyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pharmacyList = new ArrayList<>();
        adapter = new PharmacyAdapter(pharmacyList, username, prescriptionId);
        pharmacyRecyclerView.setAdapter(adapter);

        // 뒤로가기
        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(PharmacyListActivity.this, MedicalDetailActivity.class);
            backIntent.putExtra("diagnosis_id", diagnosisId);
            startActivity(backIntent);
            finish();
        });
    }

    private void fetchPharmacies(double latitude, double longitude) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    TokenManager tokenManager = new TokenManager(getApplicationContext());
                    String accessToken = tokenManager.getAccessToken();

                    Log.d("PharmacyListActivity", "Fetching pharmacies with lat=" + latitude + ", lng=" + longitude);
                    String urlStr = "http://43.201.73.161:5000/pharmacies/nearby-info?lat="
                            + latitude + "&lng=" + longitude;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setRequestProperty("Accept", "application/json");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("PharmacyListActivity", "Response: " + response.toString());

                        JSONArray pharmaciesArray = new JSONArray(response.toString());
                        Log.d("PharmacyListActivity", "Number of pharmacies received: " + pharmaciesArray.length());

                        pharmacyList.clear();
                        for (int i = 0; i < pharmaciesArray.length(); i++) {
                            JSONObject pharmacyJson = pharmaciesArray.getJSONObject(i);
                            Log.d("PharmacyListActivity", "Parsing pharms : " + pharmacyJson.toString());
                            String name = pharmacyJson.getString("pharmacy_name");
                            String address = pharmacyJson.getString("address");
                            String openHour = pharmacyJson.getString("open_hour");
                            String closeHour = pharmacyJson.getString("close_hour");
                            String contact = pharmacyJson.getString("contact");
                            Log.e("PharmacyListActivity", "pharmacyJson : " + pharmacyJson.toString());
                            int pharmacyId = -1;
                            if (pharmacyJson.has("pharmacy_id")) {
                                String pharmacyStr = pharmacyJson.getString("pharmacy_id");
                                try {
                                    pharmacyId = Integer.parseInt(pharmacyStr);
                                } catch (NumberFormatException e) {
                                    Log.e("PharmacyListActivity", "Invalid pharmacy_id format: " + pharmacyStr);
                                }
                            } else {
                                Log.e("PharmacyListActivity", "pharmacyJson에 pharmacy_id 없음: " + pharmacyJson.toString());
                            }

                            pharmacyList.add(new Pharmacy(pharmacyId, address, name, contact, openHour, closeHour));
                        }

                        runOnUiThread(() -> {
                            Log.d("PharmacyListActivity", "Successfully fetched pharm data, updating adapter");
                            Log.d("PharmacyListActivity", "pharmList size after fetch: " + pharmacyList.size());
                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        Log.e("API_ERROR", "HTTP error code: " + responseCode);
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    runOnUiThread(() -> Log.e("PharmacyListActivity", "Exception in fetchPharms: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

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
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Save to static fields so adapter can access
                staticLatitude = latitude;
                staticLongitude = longitude;
                updateLocationTextWithAddress(location);
                fetchPharmacies(latitude, longitude);
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
                // Strip "대한민국 " prefix if present
                if (address != null && address.startsWith("대한민국 ")) {
                    address = address.substring("대한민국 ".length());
                }
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
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                locationText.setText(selectedAddress);
            }
            Log.d("PharmacyListActivity", "Selected location: " + selectedAddress + ", lat: " + latitude + ", lng: " + longitude);
            if(latitude != 0 && longitude != 0) {
                fetchPharmacies(latitude, longitude);
            } else {
                Log.e("PharmacyListActivity", "Invalid coordinates, fetchPharms not called");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (staticLatitude != 0 && staticLongitude != 0) {
            latitude = staticLatitude;
            longitude = staticLongitude;

            // Explicitly update locationText UI even if not fetching new GPS
            Location fakeLocation = new Location("");
            fakeLocation.setLatitude(latitude);
            fakeLocation.setLongitude(longitude);
            updateLocationTextWithAddress(fakeLocation);
        }

        // If returning from CareRequestActivity, close immediately
        if (getIntent().getBooleanExtra("finish_on_resume", false)) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
        outState.putBoolean("isLocationFromMap", isLocationFromMap);
    }
}