package com.dormitory.management.service;

import com.dormitory.management.model.StudentProfile;
import com.dormitory.management.util.OperationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StudentProfileServiceTest {

    private final StudentProfileService service = new StudentProfileService();

    @Test
    void saveProfileShouldRejectInvalidYear() {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(1L);
        profile.setFaculty("Computer Science");
        profile.setYearOfStudy(9);

        OperationResult result = service.saveProfile(profile);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.getMessage().contains("Year of study"));
    }

    @Test
    void saveProfileShouldRejectMissingFaculty() {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(1L);
        profile.setYearOfStudy(2);

        OperationResult result = service.saveProfile(profile);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertTrue(result.getMessage().contains("Faculty"));
    }
}
