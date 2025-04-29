package com.example.silmedy.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.ui.care_request.CareRequestActivity;
import com.example.silmedy.R;
import com.example.silmedy.entity.Doctor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * DoctorListActivity에서 사용하는 RecyclerView 어댑터
 * - item_doctor.xml 레이아웃을 각 항목에 바인딩
 * - Doctor 모델 리스트를 기반으로 의사 정보를 출력
 */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    public final List<Doctor> doctorList;
    public final String username;
    public final String email;
    public final ArrayList<String> part;
    public final ArrayList<String> symptom;

    // 생성자: 의사 리스트와 사용자 정보만 주입받음
    public DoctorAdapter(List<Doctor> doctorList, String username, String email, ArrayList<String> part, ArrayList<String> symptom) {
        this.doctorList = doctorList;
        this.username = username;
        this.email = email;
        this.part = part;
        this.symptom = symptom;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML 레이아웃을 inflate하여 ViewHolder 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        // 리스트에서 해당 위치의 Doctor 객체 가져오기
        Doctor doctor = doctorList.get(position);

        // 뷰에 데이터 바인딩
        holder.name.setText(doctor.getName());
        holder.center.setText(doctor.getCenter());
        Map<String, String> scheduleMap = doctor.getSchedule();
        Calendar calendar = java.util.Calendar.getInstance();
        String[] weekdays = {"일", "월", "화", "수", "목", "금", "토"};

        int todayIdx = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1;

        int firstIdx, secondIdx;

        if (todayIdx == 6) { // Saturday
            firstIdx = 1; // Monday
            secondIdx = 2; // Tuesday
        } else if (todayIdx == 0) { // Sunday
            firstIdx = 1; // Monday
            secondIdx = 2; // Tuesday
        } else if (todayIdx == 5) { // Friday
            firstIdx = 5; // Friday
            secondIdx = 1; // Monday
        } else {
            firstIdx = todayIdx;
            secondIdx = (todayIdx + 1) % 7;
            if (secondIdx == 0 || secondIdx == 6) { // if next is Sunday or Saturday
                secondIdx = (secondIdx == 6) ? 1 : 2; // skip to Monday or Tuesday
            }
        }

        String firstDay = weekdays[firstIdx];
        String secondDay = weekdays[secondIdx];

        String firstSchedule = scheduleMap.getOrDefault(firstDay, "휴진");
        String secondSchedule = scheduleMap.getOrDefault(secondDay, "휴진");

        StringBuilder filteredSchedule = new StringBuilder();
        filteredSchedule.append(firstDay).append(" : ").append(firstSchedule).append("\n");
        filteredSchedule.append(secondDay).append(" : ").append(secondSchedule);

        holder.schedule.setText(filteredSchedule.toString().trim());
        holder.image.setImageResource(doctor.getImageResId());

        // 아이템 클릭 시 CareRequestActivity로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CareRequestActivity.class);
            intent.putExtra("user_name", username);
            intent.putExtra("email", email);
            intent.putExtra("part", part);
            intent.putExtra("symptom", symptom);
            intent.putExtra("license_number", doctor.getLicenseNumber());
            intent.putExtra("doctor_name", doctor.getName());
            intent.putExtra("doctor_department", doctor.getDepartment());
            intent.putExtra("doctor_clinic", doctor.getCenter());
            intent.putExtra("doctor_time", (Serializable) doctor.getSchedule());
            intent.putExtra("doctor_image", doctor.getImageResId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();  // 리스트 크기 반환
    }

    // ViewHolder 클래스: item_doctor.xml의 뷰와 연결
    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, center, schedule;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.doctorImage);
            name = itemView.findViewById(R.id.doctorName);
            center = itemView.findViewById(R.id.doctorCenter);
            schedule = itemView.findViewById(R.id.doctorTime);
        }
    }
}