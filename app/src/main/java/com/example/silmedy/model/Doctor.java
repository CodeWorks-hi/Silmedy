package com.example.silmedy.model;

import java.util.Map;
import java.io.Serializable;

/**
 * DoctorListActivity에서 사용하는 의사 정보 모델 클래스
 * - RecyclerView에 바인딩되는 데이터 구조
 * - 이미지, 이름, 보건소명, 진료 시간 정보를 포함
 */
public class Doctor implements Serializable {

    public int license_number; // 의사 면허 번호
    public String imageUrl;       // 이미지 리소스 ID
    public String name;          // 이름
    public String center;        // 소속 보건소
    public String department;    // 진료과목
    public Map<String, String> schedule;      // 진료 가능 시간

    // 생성자
    public Doctor(int license_number, String imageUrl, String name, String center,
                  String department, Map schedule) {
        this.license_number = license_number;
        this.imageUrl = imageUrl;
        this.name = name;
        this.center = center;
        this.department = department;
        this.schedule = schedule;
    }

    // getter
    public int getLicenseNumber() {return license_number; }
    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getCenter() {
        return center;
    }
    public String getDepartment() { return department; }

    public Map getSchedule() {return schedule; }
}