package com.example.silmedy.llama;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Firestore에 저장/조회할 채팅 메시지 모델
 * └ consult_text/{patientId}/chats/{chat_id}
 *
 * 필드(문서 필드 이름):
 *   chat_id      : 문서 ID (String)
 *   created_at   : 생성 시각 ("yyyy-MM-dd HH:mm:ss")
 *   is_separator : 날짜 구분선 여부 (boolean)
 *   patientId    : 발신자 ("나" 또는 "AI")
 *   text         : 메시지 내용 (String)
 */
public class Message {
    public String senderId;
    public String text;
    public boolean isSeparator;
    public String chatId;
    public String createdAt;

    // Firestore 역직렬화용 기본 생성자
    public Message() { }


    public Message(String senderId, String text, String createdAt, boolean isSeparator, String chatId) {
        this.senderId = senderId;
        this.text = text;
        this.createdAt = createdAt;
        this.isSeparator = isSeparator;
        this.chatId = chatId;
    }

    // 현재 시각을 "yyyy-MM-dd HH:mm:ss" 형식으로 반환
    private static String formatNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    /**
     * 두 타임스탬프가 같은 날짜인지 비교
     */
    public static boolean isSameDay(long ts1, long ts2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(new Date(ts1)).equals(fmt.format(new Date(ts2)));
    }


}
