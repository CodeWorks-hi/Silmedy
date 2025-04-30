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
    private String chat_id;
    private String created_at;
    private boolean is_separator;
    private String patientId;
    private String text;

    // Firestore 역직렬화용 기본 생성자
    public Message() { }

    /**
     * 일반 메시지 생성자
     * @param chatId    문서 ID
     * @param patientId 발신자 ("나" 또는 "AI")
     * @param text      메시지 내용
     */
    public Message(String chatId, String patientId, String text) {
        this.chat_id      = chatId;
        this.patientId    = patientId;
        this.text         = text;
        this.is_separator = false;
        this.created_at   = formatNow();
    }

    /**
     * 날짜 구분선 메시지 팩토리
     * @param timestamp 밀리초
     */
    public static Message createDateSeparator(long timestamp) {
        Message msg = new Message();
        msg.chat_id      = "";
        msg.text         = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(timestamp));
        msg.created_at   = msg.text;
        msg.is_separator = true;
        msg.patientId    = "";
        return msg;
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

    // --- getters & setters ---
    public String getChat_id() { return chat_id; }
    public void setChat_id(String chat_id) { this.chat_id = chat_id; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public boolean isIs_separator() { return is_separator; }
    public void setIs_separator(boolean is_separator) { this.is_separator = is_separator; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
