package com.dormitory.management.model;

import java.time.LocalDateTime;

public class AdminComplaintView {
    private final Long complaintId;
    private final String dormitoryNumber;
    private final String message;
    private final LocalDateTime createdAt;

    public AdminComplaintView(Long complaintId, String dormitoryNumber, String message, LocalDateTime createdAt) {
        this.complaintId = complaintId;
        this.dormitoryNumber = dormitoryNumber;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public String getDormitoryNumber() {
        return dormitoryNumber;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
