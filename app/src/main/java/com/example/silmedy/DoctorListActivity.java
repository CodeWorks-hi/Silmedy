package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.silmedy.MapActivity;
import com.example.silmedy.R;
import com.example.silmedy.adapter.DoctorAdapter;
import com.example.silmedy.model.Doctor;

import java.util.ArrayList;
import java.util.List;

/**
 * DoctorListActivity
 * - activity_doctor_list.xml 기준 ID 100% 일치
 * - 카카오맵으로 위치 선택 연동
 */
public class DoctorListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MAP = 1001;

    private ImageButton btnBack;
    private TextView locationText, btnChangeLocation;
    private Button btnSort, btnGenderFilter;
    private RecyclerView doctorRecyclerView;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        // 뷰 바인딩 (XML ID 기준)
        btnBack = findViewById(R.id.btnBack);
        locationText = findViewById(R.id.locationText);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnSort = findViewById(R.id.btnSort);
        btnGenderFilter = findViewById(R.id.btnGenderFilter);
        doctorRecyclerView = findViewById(R.id.doctorRecyclerView);

        // 뒤로가기 버튼 기능
        btnBack.setOnClickListener(v -> finish());

        // 위치 변경 → 카카오맵 액티비티 실행
        btnChangeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorListActivity.this, MapActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        });

        // RecyclerView 설정
        doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 샘플 데이터: 의사 목록 (하드코딩)
        doctorList = new ArrayList<>();
        doctorList.add(new Doctor(R.drawable.doc, "김정훈", "분당구보건소", "진료 가능 (수) 09:00 ~ 18:00"));
        doctorList.add(new Doctor(R.drawable.doc, "박지윤", "수정구보건소", "진료 가능 (금) 13:00 ~ 17:00"));
        doctorList.add(new Doctor(R.drawable.doc, "이상우", "중원구보건소", "진료 가능 (화) 10:00 ~ 16:00"));

        adapter = new DoctorAdapter(doctorList);
        doctorRecyclerView.setAdapter(adapter);
    }

    // 카카오맵에서 선택한 주소 수신 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            String selectedAddress = data.getStringExtra("selected_address");
            if (selectedAddress != null) {
                locationText.setText(selectedAddress);
            }
        }
    }
}