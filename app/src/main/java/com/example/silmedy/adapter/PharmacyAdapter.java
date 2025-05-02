package com.example.silmedy.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.silmedy.R;
import com.example.silmedy.model.Pharmacy;
import com.example.silmedy.ui.care_request.CareRequestActivity;
import com.example.silmedy.ui.prescription.PharmacyCompletedActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {
    public final List<Pharmacy> pharmacyList;
    public final String username;
    public final String prescriptionId;


    // 생성자: 의사 리스트와 사용자 정보만 주입받음
    public PharmacyAdapter(List<Pharmacy> pharmacyList, String username, String prescriptionId) {
        this.pharmacyList = pharmacyList;
        this.username = username;
        this.prescriptionId = prescriptionId;
    }

    @NonNull
    @Override
    public PharmacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML 레이아웃을 inflate하여 ViewHolder 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pharmacy, parent, false);
        return new PharmacyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyViewHolder holder, int position) {
        // 리스트에서 해당 위치의 Doctor 객체 가져오기
        Pharmacy pharmacy = pharmacyList.get(position);

        // Log statements before binding views
        Log.d("PharmacyAdapter", "Binding position: " + position);
        Log.d("PharmacyAdapter", "Pharmacy name: " + pharmacy.getName());
        Log.d("PharmacyAdapter", "Pharmacy ID: " + pharmacy.getPharmcyId());
        Log.d("PharmacyAdapter", "Pharmacy address: " + pharmacy.getAddress().toString());
        Log.d("PharmacyAdapter", "Pharmacy contact: " + pharmacy.getContact());
        Log.d("PharmacyAdapter", "Pharmacy Schedule: " + pharmacy.getOpenHour() + " ~ " + pharmacy.getCloseHour());

        // 뷰에 데이터 바인딩
        holder.name.setText(pharmacy.getName());
        holder.address.setText(pharmacy.getAddress());
        holder.hour.setText(pharmacy.getOpenHour() + " - " + pharmacy.getCloseHour());

        // 아이템 클릭 시 CareRequestActivity로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PharmacyCompletedActivity.class);
            intent.putExtra("user_name", username);
            intent.putExtra("pharmacy_name", pharmacy.getName());
            intent.putExtra("pharmacy_contact", pharmacy.getContact());
            intent.putExtra("pharmacy_id", pharmacy.getPharmcyId());
            intent.putExtra("prescription_id", prescriptionId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pharmacyList.size();  // 리스트 크기 반환
    }

    // ViewHolder 클래스: item_doctor.xml의 뷰와 연결
    public static class PharmacyViewHolder extends RecyclerView.ViewHolder {
        TextView name, hour, address;

        public PharmacyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pharmacyName);
            hour = itemView.findViewById(R.id.pharmacyHour);
            address = itemView.findViewById(R.id.pharmacyAddress);
        }
    }
}
