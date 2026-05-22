package com.dormitory.management.dao;

import com.dormitory.management.model.AdminComplaintView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComplaintDao.class);

    public boolean createComplaint(long userId, long dormitoryId, String message) {
        String sql = "INSERT INTO complaints (user_id, dormitory_id, subject, message, status) "
                + "VALUES (?, ?, 'General Complaint', ?, 'OPEN')";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, dormitoryId);
            statement.setString(3, message);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed creating complaint for user {}", userId, ex);
            return false;
        }
    }

    public List<AdminComplaintView> findAllForAdmin() {
        String sql = "SELECT c.id, c.message, c.created_at, d.dorm_number "
                + "FROM complaints c "
                + "LEFT JOIN dormitories d ON d.id = c.dormitory_id "
                + "ORDER BY c.created_at DESC";

        List<AdminComplaintView> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Timestamp createdAt = rs.getTimestamp("created_at");
                rows.add(new AdminComplaintView(
                        rs.getLong("id"),
                        rs.getString("dorm_number"),
                        rs.getString("message"),
                        createdAt == null ? null : createdAt.toLocalDateTime()));
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading complaints for admin", ex);
        }

        return rows;
    }
}
