// MessageAdapter.java
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

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ME = 0;
    private static final int TYPE_AI = 1;

    private final Context context;
    private final List<Message> msgs;
    private final String currentUser;

    public MessageAdapter(Context context, List<Message> messageList, String currentUser) {
        this.context = context;
        this.msgs = messageList;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        Message m = msgs.get(position);
        if ("나".equals(m.senderId)) {
            return TYPE_ME;
        } else {
            return TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ME) {
            return new MeHolder(
                    inflater.inflate(R.layout.message_item_me, parent, false)
            );
        } else {
            return new AiHolder(
                    inflater.inflate(R.layout.message_item_llama, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position
    ) {
        Message m = msgs.get(position);
        String text = m.text;
        String time = m.createdAt;

        if (holder instanceof MeHolder) {
            MeHolder h = (MeHolder) holder;
            h.msg.setText(text);
            h.time.setText(time);
        } else {
            AiHolder h = (AiHolder) holder;
            h.msg.setText(text);
            h.time.setText(time);
            // AI 프로필 이미지는 XML에 지정된 drawable 그대로 사용
        }
    }

    @Override
    public int getItemCount() {
        return msgs != null ? msgs.size() : 0;
    }

    static class MeHolder extends RecyclerView.ViewHolder {
        final TextView msg, time;
        MeHolder(View v) {
            super(v);
            msg  = v.findViewById(R.id.textMessage);
            time = v.findViewById(R.id.textTime);
        }
    }

    static class AiHolder extends RecyclerView.ViewHolder {
        final TextView msg, time;
        final ImageView profile;
        AiHolder(View v) {
            super(v);
            msg     = v.findViewById(R.id.textMessage);
            time    = v.findViewById(R.id.textTime);
            profile = v.findViewById(R.id.imageProfile);
        }
    }
}
