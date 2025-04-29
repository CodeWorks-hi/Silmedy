package com.example.silmedy.entity;

public class TimeSlot {
    private String time;
    private boolean enabled;
    private boolean selected;

    public TimeSlot(String time, boolean enabled) {
        this.time = time;
        this.enabled = enabled;
        this.selected = false;
    }

    public String getTime() { return time; }
    public boolean isEnabled() { return enabled; }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}