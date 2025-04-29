package com.example.silmedy.llama;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String sender;          // 발신자 ("나" 또는 "AI"), 구분선일 땐 빈 문자열
    private String text;            // 메시지 내용, 구분선일 땐 날짜 문자열
    private long timestamp;         // 타임스탬프(밀리초)
    private boolean isDateSeparator; // 날짜 구분선 여부
    private String type;            // "text" 또는 "separator"

    // 기본 생성자 (Firestore 역직렬화용)
    public Message() { }

    /**
     * 일반 텍스트 메시지용 생성자
     * @param sender 발신자("나" 또는 "AI")
     * @param text   메시지
     * @param timestamp millis
     */
    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.isDateSeparator = false;
        this.type = "text";
    }

    /**
     * 날짜 구분선 메시지(날짜 헤더) 생성 팩토리
     * @param timestamp 해당 날짜의 millis
     */
    public static Message createDateSeparator(long timestamp) {
        Message msg = new Message();
        msg.sender = "";
        // text 필드에 yyyy-MM-dd 로 날짜 포맷
        msg.text = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(timestamp));
        msg.timestamp = timestamp;
        msg.isDateSeparator = true;
        msg.type = "separator";
        return msg;
    }

    // --- getters & setters ---
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDateSeparator() {
        return isDateSeparator;
    }
    public void setDateSeparator(boolean dateSeparator) {
        isDateSeparator = dateSeparator;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
