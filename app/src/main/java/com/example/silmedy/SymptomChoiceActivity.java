package com.example.silmedy;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SymptomChoiceActivity extends AppCompatActivity {

    // 증상별 카드 View 객체
    public LinearLayout cardCold;
    public LinearLayout cardIndigestion;
    public LinearLayout cardHeat;
    public LinearLayout cardHeadache;
    public LinearLayout cardDiabetes;
    public LinearLayout cardHighBlood;

    // 뒤로가기 버튼
    public ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_choice);

//        // 증상별 카드 View 객체 바인딩
//        // 감기
//        cardCold = findViewById(R.id.cardCold);
//
//        // 소화불량
//        cardIndigestion = findViewById(R.id.cardIndigestion);
//
//        // 열
//        cardHeat = findViewById(R.id.cardHeat);
//
//        // 두통
//        cardHeadache = findViewById(R.id.cardHeadache);
//
//        // 당뇨
//        cardDiabetes = findViewById(R.id.cardDiabetes);
//
//        // 고혈압
//        cardHighBlood = findViewById(R.id.cardHighBlood);
//
//        // 뒤로가기 버튼
//        btnBack = findViewById(R.id.btnBack);
    }
}