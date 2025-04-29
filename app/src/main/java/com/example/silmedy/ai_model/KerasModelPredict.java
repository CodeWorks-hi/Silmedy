package com.example.silmedy.ai_model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class KerasModelPredict {
    private Interpreter interpreter;
    private static final int IMAGE_SIZE = 224; // 224x224 입력

    public KerasModelPredict(Context context) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(loadModelFile(context, "model_unquant.tflite"), options); // tflite 모델 사용
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String predict(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteBuffer inputBuffer = preprocessImage(bitmap);

        float[][] output = new float[1][14]; // 14개 클래스
        interpreter.run(inputBuffer, output);

        int maxIdx = 0;
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > output[0][maxIdx]) {
                maxIdx = i;
            }
        }

        String[] labels = {
            "표재성", "절개성", "자상", "화상", "손발톱질환", "자가면역질환",
            "염증성질환", "혈관종양성", "색소침착", "기생충감염", "수포성피부",
            "바이러스질환", "세균감염", "정상피부"
        };

        return labels[maxIdx];
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        resized.getPixels(intValues, 0, resized.getWidth(), 0, 0, resized.getWidth(), resized.getHeight());

        for (int pixelValue : intValues) {
            byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // R
            byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);  // G
            byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);         // B
        }
        return byteBuffer;
    }
}
