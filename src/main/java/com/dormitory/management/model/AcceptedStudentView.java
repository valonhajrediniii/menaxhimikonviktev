package com.dormitory.management.model;

import java.time.LocalDateTime;

public class AcceptedStudentView {
    private final Long applicationId;
    private final Long userId;
    private final String studentName;
    private final String studentEmail;
    private final String faculty;
    private final Integer yearOfStudy;
    private final String dormitoryNumber;
    private final String roomNumber;
    private final LocalDateTime applicationDate;

    public AcceptedStudentView(
            Long applicationId,
            Long userId,
            String studentName,
            String studentEmail,
            String faculty,
            Integer yearOfStudy,
            String dormitoryNumber,
            String roomNumber,
            LocalDateTime applicationDate) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.faculty = faculty;
        this.yearOfStudy = yearOfStudy;
        this.dormitoryNumber = dormitoryNumber;
        this.roomNumber = roomNumber;
        this.applicationDate = applicationDate;
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

    public String getDormitoryNumber() {
        return dormitoryNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
}