package com.dormitory.management.model;

import java.time.LocalDateTime;

public class PendingApplicationView {
    private final Long applicationId;
    private final Long userId;
    private final String studentName;
    private final String studentEmail;
    private final String faculty;
    private final Integer yearOfStudy;
    private final LocalDateTime applicationDate;
    private final String notes;

    public PendingApplicationView(
            Long applicationId,
            Long userId,
            String studentName,
            String studentEmail,
            String faculty,
            Integer yearOfStudy,
            LocalDateTime applicationDate,
            String notes) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
        this.applicationDate = applicationDate;
        this.notes = notes;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getFaculty() {
        return faculty;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public String getNotes() {
        return notes;
    }
}
