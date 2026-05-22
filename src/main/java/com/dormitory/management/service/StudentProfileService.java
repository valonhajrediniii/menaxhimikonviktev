package com.dormitory.management.service;

import com.dormitory.management.dao.StudentProfileDao;
import com.dormitory.management.model.StudentProfile;
import com.dormitory.management.util.OperationResult;
import com.dormitory.management.util.ValidationUtil;

import java.util.Optional;

public class StudentProfileService {
    private final StudentProfileDao studentProfileDao = new StudentProfileDao();

    public Optional<StudentProfile> getByUserId(long userId) {
        return studentProfileDao.findByUserId(userId);
    }

    public OperationResult saveProfile(StudentProfile profile) {
        if (profile == null || profile.getUserId() == null) {
            return OperationResult.failure("Invalid profile payload.");
        }

        if (!ValidationUtil.hasText(profile.getFaculty())) {
            return OperationResult.failure("Faculty is required.");
        }

        if (profile.getYearOfStudy() < 1 || profile.getYearOfStudy() > 6) {
            return OperationResult.failure("Year of study must be between 1 and 6.");
        }

        boolean saved = studentProfileDao.upsert(profile);
        return saved
                ? OperationResult.success("Profile saved successfully.")
                : OperationResult.failure("Could not save profile. Check database connection.");
    }
}
