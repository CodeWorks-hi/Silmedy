package com.example.silmedy.llama;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Firestore에 저장/조회할 채팅 메시지 모델
 * └ consult_text/{patientId}/chats/{chat_id}
 *
 * 필드(문서 필드 이름):
 *   chat_id           : 문서 ID (String)
 *   created_at        : 생성 시각 ("HH:mm" 또는 전체 타임스탬프)
 *   is_separator      : 날짜 구분선 여부 (boolean)
 *   senderId          : 발신자 ("나" 또는 "AI")
 *   text              : 원문 메시지 (String)
 *   patientSymptoms   : 환자가 입력한 증상 (String)
 *   diseaseSymptoms   : 질병별 증상 목록 (String)
 *   mainSymptoms      : 주요 증상 (String)
 *   homeActions       : 자가 관리 방법 (String)
 *   guideline         : 권고사항 (String)
 *   emergencyAdvice   : 응급 조치 (String)
 */
public class Message {
    private String senderId;
    private String text;
    private boolean isSeparator;
    private String chatId;
    private String createdAt;

    private String patientSymptoms;
    private String diseaseSymptoms;
    private String mainSymptoms;
    private String homeActions;
    private String guideline;
    private String emergencyAdvice;

    // Firestore 역직렬화용 기본 생성자
    public Message() {}

    /**
     * 환자/AI 메시지 기본 생성자 (증상 필드는 빈 문자열로 초기화)
     */
    public Message(
            String senderId,
            String text,
            String createdAt,
            boolean isSeparator,
            String chatId
    ) {
        this.senderId        = senderId;
        this.text            = text;
        this.createdAt       = createdAt;
        this.isSeparator     = isSeparator;
        this.chatId          = chatId;
        this.patientSymptoms = "";
        this.diseaseSymptoms = "";
        this.mainSymptoms    = "";
        this.homeActions     = "";
        this.guideline       = "";
        this.emergencyAdvice = "";
    }

    /**
     * AI 메시지 전체 필드 생성자
     */
    public Message(
            String senderId,
            String text,
            String createdAt,
            boolean isSeparator,
            String chatId,
            String patientSymptoms,
            String diseaseSymptoms,
            String mainSymptoms,
            String homeActions,
            String guideline,
            String emergencyAdvice
    ) {
        this.senderId         = senderId;
        this.text             = text;
        this.createdAt        = createdAt;
        this.isSeparator      = isSeparator;
        this.chatId           = chatId;
        this.patientSymptoms  = patientSymptoms;
        this.diseaseSymptoms  = diseaseSymptoms;
        this.mainSymptoms     = mainSymptoms;
        this.homeActions      = homeActions;
        this.guideline        = guideline;
        this.emergencyAdvice  = emergencyAdvice;
    }

    // ===== Getters & Setters =====

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isSeparator() { return isSeparator; }
    public void setSeparator(boolean separator) { isSeparator = separator; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPatientSymptoms() { return patientSymptoms; }
    public void setPatientSymptoms(String patientSymptoms) { this.patientSymptoms = patientSymptoms; }

    public String getDiseaseSymptoms() { return diseaseSymptoms; }
    public void setDiseaseSymptoms(String diseaseSymptoms) { this.diseaseSymptoms = diseaseSymptoms; }

    public String getMainSymptoms() { return mainSymptoms; }
    public void setMainSymptoms(String mainSymptoms) { this.mainSymptoms = mainSymptoms; }

    public String getHomeActions() { return homeActions; }
    public void setHomeActions(String homeActions) { this.homeActions = homeActions; }

    public String getGuideline() { return guideline; }
    public void setGuideline(String guideline) { this.guideline = guideline; }

    public String getEmergencyAdvice() { return emergencyAdvice; }
    public void setEmergencyAdvice(String emergencyAdvice) { this.emergencyAdvice = emergencyAdvice; }

    // ===== 유틸 =====

    /** 현재 시각을 "HH:mm" 형식으로 반환 */
    public static String formatTimeOnly(long epochMillis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(epochMillis));
    }

    /** 현재 시각을 "yyyy-MM-dd HH:mm:ss" 전체 타임스탬프로 반환 */
    public static String formatFullTimestamp(long epochMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(epochMillis));
    }

    /**
     * 두 타임스탬프가 같은 날짜인지 비교
     */
    public static boolean isSameDay(long ts1, long ts2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(new Date(ts1)).equals(fmt.format(new Date(ts2)));
    }
}
