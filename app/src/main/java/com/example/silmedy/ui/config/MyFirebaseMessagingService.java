package com.example.silmedy.ui.config;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Firebase 클라우드 메시징을 처리하는 서비스 클래스
 * - 새로운 FCM 토큰 수신
 * - 수신된 메시지를 알림으로 표시
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "Slimedy_channel";
    private static final String CHANNEL_NAME = "Slimedy 알림";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "새로운 FCM 토큰: " + token);

        // 서버 또는 Firestore에 저장 가능
        // 예: FirebaseFirestore.getInstance().collection("tokens").document(token).set(...)
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCM", "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d("FCM", "Message Title: " + title);
            Log.d("FCM", "Message Body: " + body);

            showNotification(title, body);
        }
    }

    /**
     * 실제로 알림을 생성하고 표시하는 함수
     */
    private void showNotification(String title, String message) {
        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }

        // Android 13+ 알림 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("FCM", "알림 권한이 없어 표시되지 않음");
            return;
        }


    }
}
