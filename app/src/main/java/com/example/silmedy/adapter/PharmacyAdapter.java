package com.example.silmedy.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.model.Pharmacy;
import com.example.silmedy.ui.prescription.PharmacyCompletedActivity;

import java.util.List;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.PharmacyViewHolder> {
    public final List<Pharmacy> pharmacyList;
    public final String username;
    public final int prescriptionId;
    private Pharmacy selectedPharmacy;
    private final OnPharmacySelectedListener selectionListener;

    // 콜백 인터페이스: 선택된 약국을 알림
    public interface OnPharmacySelectedListener {
        void onPharmacySelected(Pharmacy selected);
    }

    // 생성자: 의사 리스트, 사용자 정보, 선택 콜백 주입
    public PharmacyAdapter(List<Pharmacy> pharmacyList, String username, int prescriptionId, OnPharmacySelectedListener listener) {
        this.pharmacyList = pharmacyList;
        this.username = username;
        this.prescriptionId = prescriptionId;
        this.selectionListener = listener;
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

        if (pharmacy.equals(selectedPharmacy)) {
            holder.itemView.setBackgroundResource(R.drawable.selected_card_border);
            holder.name.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
            holder.hour.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.address.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.itemView.setTranslationZ(8f);  // ensures this view is drawn above others
        } else {
            holder.itemView.setBackgroundResource(R.drawable.unselected_card_background);
            holder.name.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
            holder.hour.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.address.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.itemView.setTranslationZ(0f);  // reset to normal drawing order
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = pharmacyList.indexOf(selectedPharmacy);
            if (pharmacy != selectedPharmacy) {
                selectedPharmacy = pharmacy;
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected);
                }
                notifyItemChanged(position);
                if (selectionListener != null) {
                    selectionListener.onPharmacySelected(selectedPharmacy);
                }
            }
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

    public Pharmacy getSelectedPharmacy() {
        return selectedPharmacy;
    }
}
