package com.example.silmedy.llama;

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
        msg.type = "separator";
        return msg;
    }

    // Getters & Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isDateSeparator() { return isDateSeparator; }
    public void setDateSeparator(boolean dateSeparator) { isDateSeparator = dateSeparator; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
