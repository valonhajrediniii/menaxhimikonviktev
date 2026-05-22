package com.dormitory.management.dao;

import com.dormitory.management.model.StudentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StudentProfileDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentProfileDao.class);

    public Optional<StudentProfile> findByUserId(long userId) {
        String sql = "SELECT id, user_id, faculty, study_program, year_of_study, gender, phone, city, photo_url "
            + "FROM student_profiles "
            + "WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading student profile for user {}", userId, ex);
        }

        return Optional.empty();
    }

    public boolean upsert(StudentProfile profile) {
        String sql = "INSERT INTO student_profiles (user_id, faculty, study_program, year_of_study, gender, phone, city, photo_url) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (user_id) "
                + "DO UPDATE SET "
                + "faculty = EXCLUDED.faculty, "
            + "study_program = EXCLUDED.study_program, "
                + "year_of_study = EXCLUDED.year_of_study, "
                + "gender = EXCLUDED.gender, "
                + "phone = EXCLUDED.phone, "
            + "city = EXCLUDED.city, "
            + "photo_url = EXCLUDED.photo_url";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, profile.getUserId());
            statement.setString(2, profile.getFaculty());
            statement.setString(3, profile.getStudyProgram());
            statement.setInt(4, profile.getYearOfStudy());
            statement.setString(5, profile.getGender());
            statement.setString(6, profile.getPhone());
            statement.setString(7, profile.getCity());
            statement.setString(8, profile.getPhotoUrl());
            return statement.executeUpdate() > 0;
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed upserting student profile for user {}", profile.getUserId(), ex);
            return false;
        }
    }

    private StudentProfile map(ResultSet rs) throws SQLException {
        StudentProfile profile = new StudentProfile();
        profile.setId(rs.getLong("id"));
        profile.setUserId(rs.getLong("user_id"));
        profile.setFaculty(rs.getString("faculty"));
        profile.setStudyProgram(rs.getString("study_program"));
        profile.setYearOfStudy(rs.getInt("year_of_study"));
        profile.setGender(rs.getString("gender"));
        profile.setPhone(rs.getString("phone"));
        profile.setCity(rs.getString("city"));
        profile.setPhotoUrl(rs.getString("photo_url"));
        return profile;
    }
}
