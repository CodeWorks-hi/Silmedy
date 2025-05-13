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

    private static final int TYPE_GREETING = -1;
    private static final int TYPE_ME = 0;
    private static final int TYPE_AI = 1;

    private final Context context;
    private final List<Message> msgs;
    private final String currentUser;
    private boolean showGreeting = true;

    public MessageAdapter(Context context, List<Message> messageList, String currentUser) {
        this.context = context;
        this.msgs = messageList;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        if (showGreeting && position == 0) {
            return TYPE_GREETING;
        }
        int dataPos = showGreeting ? position - 1 : position;
        Message m = msgs.get(dataPos);
        return "나".equals(m.getSenderId()) ? TYPE_ME : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_GREETING) {
            View v = inflater.inflate(R.layout.message_item_greeting, parent, false);
            return new GreetingHolder(v);
        } else if (viewType == TYPE_ME) {
            View v = inflater.inflate(R.layout.message_item_me, parent, false);
            return new MeHolder(v);
        } else {
            View v = inflater.inflate(R.layout.message_item_llama, parent, false);
            return new AiHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_GREETING) {
            // greeting holder has static text, nothing to bind
            return;
        }
        int dataPos = showGreeting ? position - 1 : position;
        Message m = msgs.get(dataPos);
        String time = m.getCreatedAt();

        if (holder instanceof MeHolder) {
            MeHolder h = (MeHolder) holder;
            h.msg.setText(m.getText());
            h.time.setText(time);
        } else if (holder instanceof AiHolder) {
            AiHolder h = (AiHolder) holder;
            h.textPatientSymptoms.setText(
                    context.getString(R.string.label_patient_symptoms) + ": " + m.getPatientSymptoms()
            );
            h.textDiseaseSymptoms.setText(
                    context.getString(R.string.label_disease_symptoms) + ": " + m.getDiseaseSymptoms()
            );
            h.textMainSymptoms.setText(
                    context.getString(R.string.label_main_symptoms) + ": " + m.getMainSymptoms()
            );
            h.textHomeActions.setText(
                    context.getString(R.string.label_home_actions) + ": " + m.getHomeActions()
            );
            h.textGuideline.setText(
                    context.getString(R.string.label_guideline) + ": " + m.getGuideline()
            );
            h.textEmergencyAdvice.setText(
                    context.getString(R.string.label_emergency_advice) + ": " + m.getEmergencyAdvice()
            );
            h.textDisclaimer1.setText(context.getString(R.string.text_disclaimer1));
            h.textDisclaimer2.setText(context.getString(R.string.text_disclaimer2));
            h.textTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return msgs.size() + (showGreeting ? 1 : 0);
    }

    /** 외부에서 인사표시 토글 **/
    public void hideGreeting() {
        if (showGreeting) {
            showGreeting = false;
            notifyItemRemoved(0);
        }
    }

    static class GreetingHolder extends RecyclerView.ViewHolder {
        final TextView textGreeting;
        final ImageView profile;
        GreetingHolder(View v) {
            super(v);
            profile = v.findViewById(R.id.imageProfile);
            textGreeting = v.findViewById(R.id.textGreeting);
        }
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
        final ImageView profile;
        final TextView textPatientSymptoms;
        final TextView textDiseaseSymptoms;
        final TextView textMainSymptoms;
        final TextView textHomeActions;
        final TextView textGuideline;
        final TextView textEmergencyAdvice;
        final TextView textDisclaimer1;
        final TextView textDisclaimer2;
        final TextView textTime;

        AiHolder(View v) {
            super(v);
            profile = v.findViewById(R.id.imageProfile);
            textPatientSymptoms = v.findViewById(R.id.textPatientSymptoms);
            textDiseaseSymptoms = v.findViewById(R.id.textDiseaseSymptoms);
            textMainSymptoms = v.findViewById(R.id.textMainSymptoms);
            textHomeActions = v.findViewById(R.id.textHomeActions);
            textGuideline = v.findViewById(R.id.textGuideline);
            textEmergencyAdvice = v.findViewById(R.id.textEmergencyAdvice);
            textDisclaimer1 = v.findViewById(R.id.textDisclaimer1);
            textDisclaimer2 = v.findViewById(R.id.textDisclaimer2);
            textTime = v.findViewById(R.id.textTime);
        }
    }
}