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
        return "나".equals(m.getSenderId()) ? TYPE_ME : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ME) {
            View v = inflater.inflate(R.layout.message_item_me, parent, false);
            return new MeHolder(v);
        } else {
            View v = inflater.inflate(R.layout.message_item_llama, parent, false);
            return new AiHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position
    ) {
        Message m = msgs.get(position);
        String time = m.getCreatedAt();

        if (holder instanceof MeHolder) {
            MeHolder h = (MeHolder) holder;
            h.msg.setText(m.getText());
            h.time.setText(time);

        } else if (holder instanceof AiHolder) {
            AiHolder h = (AiHolder) holder;

            // 각 TextView에 데이터 바인딩 (레이블 + 값)
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

            // 고정된 주의 문구
            h.textDisclaimer1.setText(context.getString(R.string.text_disclaimer1));
            h.textDisclaimer2.setText(context.getString(R.string.text_disclaimer2));

            // 전송 시간
            h.textTime.setText(time);

            // 프로필 이미지는 XML drawable 그대로 사용
        }
    }

    @Override
    public int getItemCount() {
        return msgs != null ? msgs.size() : 0;
    }

    /** “나” 메시지용 ViewHolder **/
    static class MeHolder extends RecyclerView.ViewHolder {
        final TextView msg, time;
        MeHolder(View v) {
            super(v);
            msg  = v.findViewById(R.id.textMessage);
            time = v.findViewById(R.id.textTime);
        }
    }

    /** AI 메시지용 ViewHolder **/
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
            profile             = v.findViewById(R.id.imageProfile);
            textPatientSymptoms = v.findViewById(R.id.textPatientSymptoms);
            textDiseaseSymptoms = v.findViewById(R.id.textDiseaseSymptoms);
            textMainSymptoms    = v.findViewById(R.id.textMainSymptoms);
            textHomeActions     = v.findViewById(R.id.textHomeActions);
            textGuideline       = v.findViewById(R.id.textGuideline);
            textEmergencyAdvice = v.findViewById(R.id.textEmergencyAdvice);
            textDisclaimer1     = v.findViewById(R.id.textDisclaimer1);
            textDisclaimer2     = v.findViewById(R.id.textDisclaimer2);
            textTime            = v.findViewById(R.id.textTime);
        }
    }
}
