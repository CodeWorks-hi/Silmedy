package com.example.silmedy.ui.photo_clinic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.silmedy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShootingActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1000;
    private static final int REQUEST_STORAGE_PERMISSION = 500;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_ALBUM = 300;

    private LinearLayout guideOverlay;
    private Button btnHome, btnNext, btnCamera, btnAlbum,btnResult;
    private ImageView btnBack, imageView;

    private File photoFile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting);

        Intent intent = getIntent();
        // 사용자 이름 받기
        String username = intent.getStringExtra("user_name");
        String email = intent.getStringExtra("email");
        ArrayList<String> part = (ArrayList<String>) intent.getSerializableExtra("part");

        // 뷰 연결
        imageView = findViewById(R.id.imageView);
        guideOverlay = findViewById(R.id.guideOverlay);
        btnHome = findViewById(R.id.btnHome);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnCamera = findViewById(R.id.btnCamera);
        btnAlbum = findViewById(R.id.btnAlbum);
        btnResult = findViewById(R.id.btnResult);

        // 카메라, 앨범, 결과 확인가 버튼
        btnCamera.setOnClickListener(v -> camera());
        btnAlbum.setOnClickListener(v -> album());
        btnResult.setOnClickListener(v -> {
            Intent resultIntent = new Intent(ShootingActivity.this, DiagnosisResultsActivity.class);
            resultIntent.putExtra("user_name", username);
            resultIntent.putExtra("email", email);
            resultIntent.putExtra("part", part);
            if (photoFile != null && photoFile.exists()) {
                resultIntent.putExtra("image_path", photoFile.getAbsolutePath());
            }
            startActivity(resultIntent);
            finish();
        });

        // 가이드 카드 버튼
        btnHome.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> guideOverlay.setVisibility(View.GONE));

        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(ShootingActivity.this, BodyMain.class);
            backIntent.putExtra("user_name", username);
            startActivity(backIntent);
            finish();
        });

        // 초기 가이드 카드 보여주기
        guideOverlay.setVisibility(View.VISIBLE);
    }

    // 카메라 실행
    private void camera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        photoFile = getPhotoFile("IMG_" + timeStamp);

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.silmedy.fileprovider",
                    photoFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 앨범 실행
    private void album() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 이상
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
                Toast.makeText(this, "앨범 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                Toast.makeText(this, "앨범 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), REQUEST_ALBUM);
    }

    // 사진 파일 생성
    private File getPhotoFile(String fileName) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            camera();
        } else if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            album();
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 사진 촬영 또는 앨범 선택 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && photoFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        } else if (requestCode == REQUEST_ALBUM && resultCode == RESULT_OK && data != null) {
            Uri albumUri = data.getData();
            File albumFile = new File(getCacheDir(), "album_image.jpg");
            loadImageFromUri(albumUri);
            photoFile = albumFile;
        }
    }

    // 앨범 이미지 불러오기
    private void loadImageFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(new File(getCacheDir(), "album_image.jpg"))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            Bitmap bitmap = BitmapFactory.decodeFile(getCacheDir() + "/album_image.jpg");
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}