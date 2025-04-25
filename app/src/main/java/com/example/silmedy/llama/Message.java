package com.example.silmedy.llama;

import com.google.firebase.firestore.PropertyName;

public class Message {
    public  String sender;

    public String text;
    public  long timestamp;
    public boolean isDateSeparator;


    //  프로필 이미지 URL
    public  String profileImageUrl;

    //  이미지 전송 기능을 위한 추가 필드
    public  String type;

    //  기본 생성자 (Firestore에서 필요)
    public Message() {
    }

    //  일반 텍스트 메시지 생성자
    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.isDateSeparator = false;
        this.type = "text"; // 기본값은 텍스트 메시지
    }

    //  날짜 구분용 메시지 생성자
    public static Message createDateSeparator(long timestamp) {
        Message msg = new Message();
        msg.sender = "";
        msg.text = "";
        msg.timestamp = timestamp;
        msg.isDateSeparator = true;
        return msg;
    }

    // ✅ Getter & Setter
    @PropertyName("sender")
    public String getSender() {
        return sender;
    }

    @PropertyName("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @PropertyName("text")
    public String getText() {
        return text;
    }

    @PropertyName("text")
    public void setText(String text) {
        this.text = text;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("isDateSeparator")
    public boolean isDateSeparator() {
        return isDateSeparator;
    }

    @PropertyName("isDateSeparator")
    public void setDateSeparator(boolean dateSeparator) {
        isDateSeparator = dateSeparator;
    }



    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

}
