package com.example.silmedy.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.model.MedicalHistoryItem;
import com.example.silmedy.ui.user.MedicalDetailActivity;

import java.io.Serializable;
import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    private List<MedicalHistoryItem> medicalHistoryList;

    public MedicalHistoryAdapter(List<MedicalHistoryItem> medicalHistoryList) {
        this.medicalHistoryList = medicalHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalHistoryItem item = medicalHistoryList.get(position);
        holder.textDate.setText(item.getDate());
        holder.textHospital.setText(item.getHospitalName());
        String summary = item.getDiagnosisSummary().toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", ", ")
                .replace("\"", "")
                .trim();
        holder.textDiagnosis.setText("진료 정보 : " + summary);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MedicalDetailActivity.class);
            intent.putExtra("date", item.getDate());
            intent.putExtra("hospital_name", item.getHospitalName());
            intent.putExtra("diagnosis_id", item.getDiagnosisId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return medicalHistoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textHospital, textDiagnosis;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.txtMedicalDate);
            textHospital = itemView.findViewById(R.id.txtMedicalCenter);
            textDiagnosis = itemView.findViewById(R.id.txtMedical);


        }
    }
}