package com.example.silmedy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silmedy.R;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private final List<String> timeList;
    private int selectedPosition = -1;
    private final OnTimeSelectedListener listener;

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    public TimeSlotAdapter(List<String> timeList, OnTimeSelectedListener listener) {
        this.timeList = timeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_slot_item, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.timeSlotButton.setText(time);

        boolean isSelected = (position == selectedPosition);
        holder.timeSlotButton.setSelected(isSelected);
        holder.timeSlotButton.setEnabled(!time.contains("(예약됨)")); // 예약된 항목은 비활성화

        holder.timeSlotButton.setOnClickListener(v -> {
            if (holder.timeSlotButton.isEnabled()) {
                int previousPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition(); // 위치 안전하게 갱신
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                listener.onTimeSelected(time);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        Button timeSlotButton;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotButton = itemView.findViewById(R.id.timeSlotButton);
        }
    }
}