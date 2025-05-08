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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.silmedy.MainActivity;
import com.example.silmedy.R;
import com.example.silmedy.model.Doctor;
import com.example.silmedy.adapter.DoctorAdapter;
import com.example.silmedy.ui.clinic.ClinicHomeActivity;
import com.example.silmedy.ui.config.TokenManager;
import com.example.silmedy.ui.open_api.MapActivity;
import com.example.silmedy.ui.photo_clinic.BodyMain;
import com.example.silmedy.ui.photo_clinic.ShootingActivity;
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

    // Static variables to persist location between activity instances
    public static double staticLatitude = 0;
    public static double staticLongitude = 0;

    private String username, department;
    private ArrayList<String> part, symptom;

    private static final int REQUEST_CODE_MAP = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;

    private ImageButton btnBack;
    private TextView locationText, btnChangeLocation;
    private Button btnGenderFilter;
    private RecyclerView doctorRecyclerView;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;
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
        setContentView(R.layout.activity_doctor_list);

        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude", 0);
            longitude = savedInstanceState.getDouble("longitude", 0);
            isLocationFromMap = savedInstanceState.getBoolean("isLocationFromMap", false);
        } else {
            latitude = staticLatitude;
            longitude = staticLongitude;
        }

        Intent intent = getIntent();
        part = (ArrayList<String>) intent.getSerializableExtra("part");
        symptom = (ArrayList<String>) intent.getSerializableExtra("symptom");
        username = intent.getStringExtra("user_name");
        department = intent.getStringExtra("department");

        btnBack = findViewById(R.id.btnBack);
        locationText = findViewById(R.id.locationText);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnGenderFilter = findViewById(R.id.btnGenderFilter);
        doctorRecyclerView = findViewById(R.id.doctorRecyclerView);

        // 클릭 리스너 등록
        btnGenderFilter.setOnClickListener(v -> {
            final String[] genderOptions = {"전체", "남", "여"};

            AlertDialog.Builder builder = new AlertDialog.Builder(DoctorListActivity.this);
            builder.setTitle("성별 필터 선택")
                    .setItems(genderOptions, (dialog, which) -> {
                        // 선택된 항목 처리
                        String selectedGender = genderOptions[which];
                        btnGenderFilter.setText("성별 : " + selectedGender);

                        // 선택값에 따라 필터 로직 수행
                        filterDoctorsByGender(selectedGender);
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 클라이언트 초기화 및 위치 가져오기
        if (!isLocationFromMap && latitude == 0 && longitude == 0) {
            checkLocationPermissionAndGetCurrentLocation();
            // fetchDoctors will be called after location is fetched in getCurrentLocation()
        } else {
            // Use restored or map location
            fetchDoctors(latitude, longitude, department);
        }

        // 위치 변경 클릭 시 카카오맵 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent kakaoIntent = new Intent(DoctorListActivity.this, MapActivity.class);
            startActivityForResult(kakaoIntent, REQUEST_CODE_MAP);
        });

        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(doctorList, username, part, symptom);
        doctorRecyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            if (department != null && department.equals("내과")) {
                Intent backIntent = new Intent(DoctorListActivity.this, SymptomChoiceActivity.class);
                backIntent.putExtra("user_name", username);
                backIntent.putExtra("part", part);
                backIntent.putExtra("symptom", symptom);
                startActivity(backIntent);
            } else if (department != null && department.equals("외과")) {
                Intent backIntent = new Intent(DoctorListActivity.this, BodyMain.class);
                backIntent.putExtra("user_name", username);
                backIntent.putExtra("part", part);
                backIntent.putExtra("symptom", symptom);
                startActivity(backIntent);
            }
            finish();
        });
    }

    private void filterDoctorsByGender(String selectedGender) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Log.d("DoctorListActivity", "Fetching doctors with lat=" + latitude + ", lng=" + longitude + ", department=" + department);
                    String urlStr = "http://43.201.73.161:5000/health-centers-with-doctors?lat="
                            + latitude + "&lng=" + longitude + "&department=" + department + "&gender=" + selectedGender;
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
                            int licenseNumber = -1;
                            if (doctorJson.has("license_number")) {
                                String licenseStr = doctorJson.getString("license_number");
                                try {
                                    licenseNumber = Integer.parseInt(licenseStr);
                                } catch (NumberFormatException e) {
                                    Log.e("DoctorListActivity", "Invalid license_number format: " + licenseStr);
                                }
                            } else {
                                Log.e("DoctorListActivity", "doctorJson에 license_number 없음: " + doctorJson.toString());
                            }

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

    private void fetchDoctors(double latitude, double longitude, String department) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Log.d("DoctorListActivity", "Fetching doctors with lat=" + latitude + ", lng=" + longitude + ", department=" + department);
                    String urlStr = "http://43.201.73.161:5000/health-centers-with-doctors?lat="
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
                            int licenseNumber = -1;
                            if (doctorJson.has("license_number")) {
                                String licenseStr = doctorJson.getString("license_number");
                                try {
                                    licenseNumber = Integer.parseInt(licenseStr);
                                } catch (NumberFormatException e) {
                                    Log.e("DoctorListActivity", "Invalid license_number format: " + licenseStr);
                                }
                            } else {
                                Log.e("DoctorListActivity", "doctorJson에 license_number 없음: " + doctorJson.toString());
                            }

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
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Save to static fields so adapter can access
                staticLatitude = latitude;
                staticLongitude = longitude;
                updateLocationTextWithAddress(location);
                if(department != null && !department.isEmpty()) {
                    fetchDoctors(latitude, longitude, department);
                }
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
            isLocationFromMap = true;
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            // Store statically as well
            staticLatitude = latitude;
            staticLongitude = longitude;
            String selectedAddress = data.getStringExtra("selected_address");
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                locationText.setText(selectedAddress);
            }
            Log.d("DoctorListActivity", "Selected location: " + selectedAddress + ", lat: " + latitude + ", lng: " + longitude);
            if(latitude != 0 && longitude != 0 && department != null && !department.isEmpty()) {
                fetchDoctors(latitude, longitude, department);
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