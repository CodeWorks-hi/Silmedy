package com.example.silmedy.llama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final ArrayList<Message> messageList = new ArrayList<>();
    private final String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context = context;
        if (messageList != null) {
            this.messageList.addAll(messageList);
        }
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.isDateSeparator()) return 2;
        return msg.getSender().equals(currentUser) ? 0 : 1;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_me, parent, false);
        } else if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_llama, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_date, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);

        if (msg.isDateSeparator()) {
            holder.textDate.setText(getDate(msg.getTimestamp()));
            return;
        }

        holder.textSender.setText(msg.getSender());
        holder.textTime.setText(getTime(msg.getTimestamp()));
        holder.textMessage.setText(msg.getText());
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    private String getTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    private String getDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime, textDate;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView.findViewById(R.id.textSender) != null)
                textSender = itemView.findViewById(R.id.textSender);
            if (itemView.findViewById(R.id.textMessage) != null)
                textMessage = itemView.findViewById(R.id.textMessage);
            if (itemView.findViewById(R.id.textTime) != null)
                textTime = itemView.findViewById(R.id.textTime);
            if (itemView.findViewById(R.id.textDate) != null)
                textDate = itemView.findViewById(R.id.textDate);
        }
    }
}