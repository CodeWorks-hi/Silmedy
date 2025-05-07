package com.example.silmedy.ui.config;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase 클라우드 메시징을 처리하는 서비스 클래스
 * - FCM 메시지를 수신해서 Cloud Firestore에 바로 저장
 */
public class MyFirebaseChatMessagingService extends FirebaseMessagingService {

    public static final String TAG = "FCMService";
    public static final String COLLECTION_CHAT = "chatMessages";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // 1) 페이로드에서 메시지 추출
        String body = null;
        if (remoteMessage.getNotification() != null) {
            body = remoteMessage.getNotification().getBody();
        } else if (!remoteMessage.getData().isEmpty()) {
            // 예: data payload에 "message" 키로 보냈다면
            body = remoteMessage.getData().get("message");
        }

        if (body == null) {
            Log.w(TAG, "No message payload, skip saving");
            return;
        }

        // 2) Firestore에 저장할 Map 또는 모델 객체 생성
        long timestamp = System.currentTimeMillis();
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("sender", remoteMessage.getFrom());   // 혹은 사용자 식별 ID
        chatData.put("message", body);
        chatData.put("timestamp", timestamp);

        // 3) Firestore 인스턴스 얻고 컬렉션에 저장
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_CHAT)
                .add(chatData)
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Chat saved, id=" + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving chat", e));
    }
}
