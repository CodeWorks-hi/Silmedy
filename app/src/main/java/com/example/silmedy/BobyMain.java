package com.example.silmedy;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class BobyMain extends AppCompatActivity {

    ImageView bodyImage;
    LinearLayout symptomButtons;

    enum BodyStage {
        BODY, ARM, HAND
    }

    BodyStage currentStage = BodyStage.BODY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boby_main);

        bodyImage = findViewById(R.id.bodyImage);
        symptomButtons = findViewById(R.id.symptomButtons);

        updateStageImage();

        bodyImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();

                switch (currentStage) {
                    case BODY:
                        if (isInArmArea(x, y, v)) {
                            currentStage = BodyStage.ARM;
                            updateStageImage();
                        }
                        break;
                    case ARM:
                        if (isInHandArea(x, y, v)) {
                            currentStage = BodyStage.HAND;
                            updateStageImage();
                            symptomButtons.setVisibility(View.VISIBLE); // 증상 버튼 표시
                        }
                        break;
                    case HAND:
                        Toast.makeText(this, "손 이미지 클릭됨 (최종 확대)", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            return true;
        });

        findViewById(R.id.btnPain).setOnClickListener(v ->
                Toast.makeText(this, "통증 선택됨", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnSwelling).setOnClickListener(v ->
                Toast.makeText(this, "부종 선택됨", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnItch).setOnClickListener(v ->
                Toast.makeText(this, "가려움 선택됨", Toast.LENGTH_SHORT).show());
    }

    private void updateStageImage() {
        symptomButtons.setVisibility(View.GONE); // 초기에는 숨김
        switch (currentStage) {
            case BODY:
                bodyImage.setImageResource(R.drawable.boby); // 전체 몸 or 팔 위주 이미지
                break;
            case ARM:
                bodyImage.setImageResource(R.drawable.arm); // 팔 확대 이미지
                break;
            case HAND:
                bodyImage.setImageResource(R.drawable.hand); // 손 확대 이미지
                break;
        }
    }

    // 단순 예시 – 위치에 따라 조절 가능
    private boolean isInArmArea(float x, float y, View v) {
        return y >= v.getHeight() * 0.3 && y < v.getHeight() * 0.6;
    }

    private boolean isInHandArea(float x, float y, View v) {
        return y >= v.getHeight() * 0.6;
    }
}
