package com.example.silmedy.model;

public class MedicalHistoryItem {
    private String diagnosisId;
    private String date;
    private String hospitalName;
    private String diagnosisSummary;

    public MedicalHistoryItem(String diagnosisId, String date, String hospitalName, String diagnosisSummary) {
        this.diagnosisId = diagnosisId;
        this.date = date;
        this.hospitalName = hospitalName;
        this.diagnosisSummary = diagnosisSummary;
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

    public String getDiagnosisSummary() {
        return diagnosisSummary;
    }
}