package com.example.silmedy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silmedy.R;
import com.example.silmedy.llama.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ME    = 0;
    public static final int TYPE_AI    = 1;
    public static final int TYPE_DATE  = 2;

    public final Context context;
    public final ArrayList<Message> msgs;
    public final String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context     = context;
        this.msgs        = messageList;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ME) {
            return new MeHolder(inflater.inflate(R.layout.message_item_me, p, false));
        } else if (viewType == TYPE_AI) {
            return new AiHolder(inflater.inflate(R.layout.message_item_llama, p, false));
        } else {
            return new DateHolder(inflater.inflate(R.layout.message_item_date, p, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        Message m = msgs.get(pos);
        if (h instanceof DateHolder) {
            ((DateHolder) h).date.setText(formatDate(m.getTimestamp()));
        } else if (h instanceof MeHolder) {
            MeHolder vh = (MeHolder) h;
            vh.time.setText(formatTime(m.getTimestamp()));
            vh.msg.setText(m.getText());
        } else if (h instanceof AiHolder) {
            AiHolder vh = (AiHolder) h;
            // vh.sender.setText("AI"); // Disabled: textSender view not defined
            vh.time.setText(formatTime(m.getTimestamp()));
            vh.msg.setText(m.getText());
        }
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

    public String formatTime(long ts) {
        return new SimpleDateFormat("a hh:mm", Locale.KOREA).format(new Date(ts));
    }
    public String formatDate(long ts) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(new Date(ts));
    }

    static class MeHolder extends RecyclerView.ViewHolder {
        TextView msg, time;
        MeHolder(View v) {
            super(v);
            msg  = v.findViewById(R.id.textMessage);
            time = v.findViewById(R.id.textTime);
        }
    }

    static class AiHolder extends RecyclerView.ViewHolder {
        TextView sender, msg, time;
        ImageView profile;
        AiHolder(View v) {
            super(v);
            // sender  = v.findViewById(R.id.textSender); // Disabled: textSender view not defined
            msg     = v.findViewById(R.id.textMessage);
            time    = v.findViewById(R.id.textTime);
            profile = v.findViewById(R.id.imageProfile);
        }
    }

    static class DateHolder extends RecyclerView.ViewHolder {
        TextView date;
        DateHolder(View v) {
            super(v);
            date = v.findViewById(R.id.textDate);
        }
    }
}
