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
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.open_api.MapActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class DoctorListActivity extends AppCompatActivity {

    private String username, department;
    private ArrayList<String> part, symptom;

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
        new TokenManager(getApplicationContext()).refreshAccessToken();
        setContentView(R.layout.activity_doctor_list);

        Intent intent = getIntent();
        part = (ArrayList<String>) intent.getSerializableExtra("part");
        symptom = (ArrayList<String>) intent.getSerializableExtra("symptom");
        username = intent.getStringExtra("user_name");
        department = intent.getStringExtra("department");

        btnBack = findViewById(R.id.btnBack);
        locationText = findViewById(R.id.locationText);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnSort = findViewById(R.id.btnSort);
        btnGenderFilter = findViewById(R.id.btnGenderFilter);
        doctorRecyclerView = findViewById(R.id.doctorRecyclerView);

        // 위치 변경 클릭 시 카카오맵 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent kakaoIntent = new Intent(DoctorListActivity.this, MapActivity.class);
            startActivityForResult(kakaoIntent, REQUEST_CODE_MAP);
        });

        // 위치 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermissionAndGetCurrentLocation();

        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(doctorList, username, part, symptom);
        doctorRecyclerView.setAdapter(adapter);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d("DoctorListActivity", "location: " + location.getLatitude() + ", " + location.getLongitude());
                fetchDoctors(location.getLatitude(), location.getLongitude(), department);
            } else {
                Log.e("LOCATION", "Failed to get location");
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DoctorListActivity.this, SymptomChoiceActivity.class);
            backIntent.putExtra("user_name", username);
            backIntent.putExtra("part", part);
            backIntent.putExtra("symptom", symptom);
            startActivity(backIntent);
            finish();
        });
    }

    private void fetchDoctors(double latitude, double longitude, String department) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Log.d("DoctorListActivity", "Fetching doctors with lat=" + latitude + ", lng=" + longitude + ", department=" + department);
                    String urlStr = "http://192.168.0.170:5000/health-centers-with-doctors?lat="
                            + latitude + "&lng=" + longitude + "&department=" + department;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    // Authorization header removed as per instructions

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("DoctorListActivity", "Response: " + response.toString());

                        JSONArray doctorsArray = new JSONArray(response.toString());
                        Log.d("DoctorListActivity", "Number of doctors received: " + doctorsArray.length());

                        doctorList.clear();
                        for (int i = 0; i < doctorsArray.length(); i++) {
                            JSONObject doctorJson = doctorsArray.getJSONObject(i);
                            Log.d("DoctorListActivity", "Parsing doctor: " + doctorJson.toString());
                            String name = doctorJson.getString("name");
                            String center = doctorJson.getString("hospital_name");
                            String dep = doctorJson.getString("department");
                            String profileUrl = doctorJson.getString("profile_url");
                            int licenseNumber = doctorJson.getString("hospital_id").hashCode();

                            JSONObject availabilityObj = doctorJson.getJSONObject("availability");
                            HashMap<String, String> schedule = new HashMap<>();
                            Iterator<String> keys = availabilityObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                schedule.put(key, availabilityObj.getString(key));
                            }

                            doctorList.add(new Doctor(licenseNumber, profileUrl, name, center, dep, schedule));
                        }

                        runOnUiThread(() -> {
                            Log.d("DoctorListActivity", "Successfully fetched doctor data, updating adapter");
                            Log.d("DoctorListActivity", "doctorList size after fetch: " + doctorList.size());
                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        Log.e("API_ERROR", "HTTP error code: " + responseCode);
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    runOnUiThread(() -> Log.e("DoctorListActivity", "Exception in fetchDoctors: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
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
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            // department is already stored as a member variable
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                locationText.setText(selectedAddress);
            }
            Log.d("DoctorListActivity", "Selected location: " + selectedAddress + ", lat: " + latitude + ", lng: " + longitude);
            Log.d("DoctorListActivity", "Department: " + department);
            if(latitude != 0 && longitude != 0 && department != null && !department.isEmpty()) {
                fetchDoctors(latitude, longitude, department);
            } else {
                Log.e("DoctorListActivity", "Invalid coordinates or department, fetchDoctors not called");
            }
        }
    }
}