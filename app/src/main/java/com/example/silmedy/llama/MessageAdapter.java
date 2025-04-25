package com.example.silmedy.llama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ME = 0;
    public static final int TYPE_AI = 1;
    public static final int TYPE_DATE = 2;

    private final Context context;
    private final ArrayList<Message> msgs;
    private final String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context = context;
        this.msgs = messageList;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        Message m = msgs.get(position);
        if (m.isDateSeparator()) return TYPE_DATE;
        return m.getSender().equals(currentUser) ? TYPE_ME : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ME) {
            return new MeHolder(inflater.inflate(R.layout.message_item_me, parent, false));
        } else if (viewType == TYPE_AI) {
            return new AiHolder(inflater.inflate(R.layout.message_item_llama, parent, false));
        } else {
            return new DateHolder(inflater.inflate(R.layout.message_item_date, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = msgs.get(position);
        if (holder instanceof DateHolder) {
            ((DateHolder) holder).date.setText(formatDate(m.getTimestamp()));
        } else if (holder instanceof MeHolder) {
            ((MeHolder) holder).msg.setText(m.getText());
            ((MeHolder) holder).time.setText(formatTime(m.getTimestamp()));
        } else if (holder instanceof AiHolder) {
            ((AiHolder) holder).msg.setText(m.getText());
            ((AiHolder) holder).time.setText(formatTime(m.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return msgs != null ? msgs.size() : 0;
    }

    private String formatTime(long ts) {
        return new SimpleDateFormat("a hh:mm", Locale.KOREA).format(new Date(ts));
    }

    private String formatDate(long ts) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(new Date(ts));
    }

    static class MeHolder extends RecyclerView.ViewHolder {
        final TextView msg, time;
        MeHolder(View v) {
            super(v);
            msg = v.findViewById(R.id.textMessage);
            time = v.findViewById(R.id.textTime);
        }
    }

    static class AiHolder extends RecyclerView.ViewHolder {
        final TextView msg, time;
        final ImageView profile;
        AiHolder(View v) {
            super(v);
            msg = v.findViewById(R.id.textMessage);
            time = v.findViewById(R.id.textTime);
            profile = v.findViewById(R.id.imageProfile);
        }
    }

    static class DateHolder extends RecyclerView.ViewHolder {
        final TextView date;
        DateHolder(View v) {
            super(v);
            date = v.findViewById(R.id.textDate);
        }
    }
}
