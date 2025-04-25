// 경로: app/src/main/java/com/example/silmedy/llama/Message.java
package com.example.silmedy.llama;

import com.google.firebase.firestore.PropertyName;

public class Message {
    public String sender;
    public String text;
    public long timestamp;
    public boolean isDateSeparator;
    public String type;

    public Message() { }

    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.isDateSeparator = false;
        this.type = "text";
    }

    public static Message createDateSeparator(long timestamp) {
        Message msg = new Message();
        msg.sender = "";
        msg.text = "";
        msg.timestamp = timestamp;
        msg.isDateSeparator = true;
        return msg;
    }

    @PropertyName("sender")
    public String getSender() { return sender; }
    @PropertyName("sender")
    public void setSender(String sender) { this.sender = sender; }

    @PropertyName("text")
    public String getText() { return text; }
    @PropertyName("text")
    public void setText(String text) { this.text = text; }

    @PropertyName("timestamp")
    public long getTimestamp() { return timestamp; }
    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @PropertyName("isDateSeparator")
    public boolean isDateSeparator() { return isDateSeparator; }
    @PropertyName("isDateSeparator")
    public void setDateSeparator(boolean dateSeparator) { isDateSeparator = dateSeparator; }

    @PropertyName("type")
    public String getType() { return type; }
    @PropertyName("type")
    public void setType(String type) { this.type = type; }
}
