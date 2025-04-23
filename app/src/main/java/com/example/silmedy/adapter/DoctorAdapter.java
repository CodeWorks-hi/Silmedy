package com.example.silmedy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.model.Doctor;

import java.util.List;

/**
 * DoctorListActivity에서 사용하는 RecyclerView 어댑터
 * - item_doctor.xml 레이아웃을 각 항목에 바인딩
 * - Doctor 모델 리스트를 기반으로 의사 정보를 출력
 */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final List<Doctor> doctorList;

    // 생성자: 외부에서 의사 리스트를 주입받음
    public DoctorAdapter(List<Doctor> doctorList) {
        this.doctorList = doctorList;
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
        holder.schedule.setText(doctor.getSchedule());
        holder.image.setImageResource(doctor.getImageResId());
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