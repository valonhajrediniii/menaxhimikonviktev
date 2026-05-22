package com.dormitory.management.dao;

import com.dormitory.management.model.Application;
import com.dormitory.management.model.ApplicationStatus;
import com.dormitory.management.model.AcceptedStudentView;
import com.dormitory.management.model.PendingApplicationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDao.class);

    public Optional<Application> findByUserId(long userId) {
        String sql = "SELECT a.id, a.user_id, a.application_date, a.status, a.dormitory_id, a.room_id, a.notes, "
            + "d.dorm_number, r.room_number "
            + "FROM applications a "
            + "LEFT JOIN dormitories d ON d.id = a.dormitory_id "
            + "LEFT JOIN rooms r ON r.id = a.room_id "
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
            LOGGER.error("Failed loading application by user {}", userId, ex);
        }

        return Optional.empty();
    }

    public Optional<Application> findById(long applicationId) {
        String sql = "SELECT a.id, a.user_id, a.application_date, a.status, a.dormitory_id, a.room_id, a.notes, "
            + "d.dorm_number, r.room_number "
            + "FROM applications a "
            + "LEFT JOIN dormitories d ON d.id = a.dormitory_id "
            + "LEFT JOIN rooms r ON r.id = a.room_id "
            + "WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, applicationId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading application {}", applicationId, ex);
        }

        return Optional.empty();
    }

    public boolean createPending(long userId, String notes) {
        String sql = "INSERT INTO applications (user_id, status, notes) "
            + "VALUES (?, 'PENDING', ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, notes);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed creating pending application for user {}", userId, ex);
            return false;
        }
    }

    public List<PendingApplicationView> findPendingForAdmin() {
        String sql = "SELECT a.id AS application_id, "
            + "u.id AS user_id, "
            + "u.full_name, "
            + "u.email, "
            + "sp.faculty, "
            + "sp.year_of_study, "
            + "a.application_date, "
            + "a.notes "
            + "FROM applications a "
            + "JOIN users u ON u.id = a.user_id "
            + "LEFT JOIN student_profiles sp ON sp.user_id = u.id "
            + "WHERE a.status = 'PENDING' "
            + "ORDER BY a.application_date ASC";

        List<PendingApplicationView> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("application_date");
                rows.add(new PendingApplicationView(
                        rs.getLong("application_id"),
                        rs.getLong("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("faculty"),
                        (Integer) rs.getObject("year_of_study"),
                        timestamp != null ? timestamp.toLocalDateTime() : null,
                        rs.getString("notes")
                ));
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading pending applications", ex);
        }

        return rows;
    }

    public List<AcceptedStudentView> findAcceptedForAdmin() {
        String sql = "SELECT a.id AS application_id, "
            + "u.id AS user_id, "
            + "u.full_name, "
            + "u.email, "
            + "sp.faculty, "
            + "sp.year_of_study, "
            + "d.dorm_number, "
            + "r.room_number, "
            + "a.application_date "
            + "FROM applications a "
            + "JOIN users u ON u.id = a.user_id "
            + "LEFT JOIN student_profiles sp ON sp.user_id = u.id "
            + "LEFT JOIN dormitories d ON d.id = a.dormitory_id "
            + "LEFT JOIN rooms r ON r.id = a.room_id "
            + "WHERE a.status = 'ACCEPTED' "
            + "ORDER BY a.application_date DESC";

        List<AcceptedStudentView> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("application_date");
                rows.add(new AcceptedStudentView(
                        rs.getLong("application_id"),
                        rs.getLong("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("faculty"),
                        (Integer) rs.getObject("year_of_study"),
                        rs.getString("dorm_number"),
                        rs.getString("room_number"),
                        timestamp != null ? timestamp.toLocalDateTime() : null
                ));
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading accepted students", ex);
        }

        return rows;
    }

    public boolean markRejected(long applicationId) {
        String sql = "UPDATE applications SET status = 'REJECTED' WHERE id = ? AND status = 'PENDING'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, applicationId);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed rejecting application {}", applicationId, ex);
            return false;
        }
    }

    public void markAccepted(Connection connection, long applicationId, long dormitoryId, long roomId) throws SQLException {
        String sql = "UPDATE applications "
            + "SET status = 'ACCEPTED', dormitory_id = ?, room_id = ? "
            + "WHERE id = ? AND status = 'PENDING'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dormitoryId);
            statement.setLong(2, roomId);
            statement.setLong(3, applicationId);
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Application is not pending or does not exist.");
            }
        }
    }

    private Application map(ResultSet rs) throws SQLException {
        Application application = new Application();
        application.setId(rs.getLong("id"));
        application.setUserId(rs.getLong("user_id"));

        Timestamp timestamp = rs.getTimestamp("application_date");
        application.setApplicationDate(timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now());
        application.setStatus(ApplicationStatus.valueOf(rs.getString("status")));

        application.setDormitoryId((Long) rs.getObject("dormitory_id"));
        application.setRoomId((Long) rs.getObject("room_id"));
        application.setDormitoryNumber(rs.getString("dorm_number"));
        application.setRoomNumber(rs.getString("room_number"));
        application.setNotes(rs.getString("notes"));
        return application;
    }
}
