package com.example.silmedy.model;

public class MedicalHistoryItem {
    private String diagnosisId;
    private String date;
    private String hospitalName;
    private String symptoms;

    public MedicalHistoryItem(String diagnosisId, String date, String hospitalName, String symptoms) {
        this.diagnosisId = diagnosisId;
        this.date = date;
        this.hospitalName = hospitalName;
        this.symptoms = symptoms;
    }

    public String getDiagnosisId() {
        return diagnosisId;
    }

    public String getDate() {
        return date;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public String getSymptoms() {
        return symptoms;
    }
}