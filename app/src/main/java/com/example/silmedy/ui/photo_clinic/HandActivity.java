package com.example.silmedy.ui.photo_clinic;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.R;

public class HandActivity extends AppCompatActivity {

    private ImageView handImage;
    private String selectedFinger = null; // 선택된 손가락 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand);

        handImage = findViewById(R.id.handImage);

        // 🖐 손 이미지 터치 시 손가락 감지
        handImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                selectedFinger = detectFinger(x, y, v);

                if (selectedFinger != null) {
                    Toast.makeText(this, selectedFinger + " 선택됨", Toast.LENGTH_SHORT).show();

                    // 🔴 선택 강조 표시 (임시 - 전체에 적용됨, 개선 여지 있음)
                    handImage.setColorFilter(Color.argb(80, 255, 0, 0)); // 전체 필터
                }
            }
            return true;
        });

        // 🔘 증상 버튼 클릭 이벤트
        setupSymptomButton(R.id.btnScratch, "긁힌 상처");
        setupSymptomButton(R.id.btnCut, "베인 상처");
        setupSymptomButton(R.id.btnItch, "가려움");
        setupSymptomButton(R.id.btnRash, "피부발진");
    }

    // 👇 손가락 좌표 감지 로직 (왼쪽→오른쪽: 엄지~새끼)
    private String detectFinger(float x, float y, View v) {
        float width = v.getWidth();
        float height = v.getHeight();

        if (y < height * 0.5) { // 상단 손가락 영역만 허용
            if (x < width * 0.2) return "엄지";
            else if (x < width * 0.4) return "검지";
            else if (x < width * 0.6) return "중지";
            else if (x < width * 0.8) return "약지";
            else return "새끼";
        }
        return null;
    }

    // 👇 증상 버튼 클릭 처리
    private void setupSymptomButton(int buttonId, String symptom) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            if (selectedFinger != null) {
                Toast.makeText(this,
                        selectedFinger + "에 " + symptom + " 선택됨",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "먼저 다친 손가락을 선택해주세요",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
