package com.example.silmedy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ArmActivity extends AppCompatActivity {

    ImageView armImage;
    Button btnArm, btnHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm);

        // 이미지 및 버튼 참조
        armImage = findViewById(R.id.armImage);
        btnArm = findViewById(R.id.btnArm);
        btnHand = findViewById(R.id.btnHand);

        // 팔 이미지 터치 → 손 부위 클릭 시 이동
        armImage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float y = event.getY();

                // 하단 30%를 손 부위로 간주
                if (y >= v.getHeight() * 0.7) {
                    goToHandActivity();
                }
            }
            return true;
        });

        // 버튼 클릭 처리
        btnArm.setOnClickListener(v ->
                Toast.makeText(this, "팔이 선택되었습니다", Toast.LENGTH_SHORT).show());

        btnHand.setOnClickListener(v -> goToHandActivity());
    }

    private void goToHandActivity() {
        Intent intent = new Intent(ArmActivity.this, HandActivity.class);
        startActivity(intent);
    }
}
