package com.example.silmedy.model;

import java.io.Serializable;
import java.util.Map;

public class Pharmacy implements Serializable {
    public int pharmcyId; // 의사 면허 번호
    public String address;       // 이미지 리소스 ID
    public String name;          // 이름
    public String contact;        // 소속 보건소
    public String openHour;    // 진료과목
    public String closeHour;      // 진료 가능 시간

    // 생성자
    public Pharmacy(int pharmcyId, String address, String name, String contact,
                   String openHour, String closeHour) {
        this.pharmcyId = pharmcyId;
        this.address = address;
        this.name = name;
        this.contact = contact;
        this.openHour = openHour;
        this.closeHour = closeHour;
    }

    // Getter 메서드
    public int getPharmcyId() {
        return pharmcyId;
    }
    public String getAddress() {
        return address;
    }
    public String getName() {
        return name;
    }
    public String getContact() {
        return contact;
    }
    public String getOpenHour() {
        return openHour;
    }
    public String getCloseHour() {
        return closeHour;
    }
}
