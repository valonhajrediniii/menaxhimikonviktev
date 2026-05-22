package com.dormitory.management.service;

import com.dormitory.management.dao.DatabaseConnection;
import com.dormitory.management.model.Role;
import com.dormitory.management.model.User;
import com.dormitory.management.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcAuthService implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAuthService.class);

    @Override
    public Optional<User> login(String email, String password) {
        String sql = "SELECT id, full_name, student_id, email, password, role, created_at "
            + "FROM users "
            + "WHERE email = ? "
            + "LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setStudentId(resultSet.getString("student_id"));
                    user.setEmail(resultSet.getString("email"));
                    String storedPassword = resultSet.getString("password");
                    if (!PasswordUtil.matches(password, storedPassword)) {
                        return Optional.empty();
                    }
                    user.setPassword(storedPassword);
                    user.setRole(Role.valueOf(resultSet.getString("role")));

                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());

                    if (!PasswordUtil.isHashed(storedPassword)) {
                        migrateLegacyPassword(connection, user.getId(), password);
                    }

                    return Optional.of(user);
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Login failed for email {}", email, ex);
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public boolean register(String fullName, String studentId, String email, String password) {
        String sql = "INSERT INTO users (full_name, student_id, email, password, role) "
            + "VALUES (?, ?, ?, ?, 'USER')";

        String hashedPassword;
        try {
            hashedPassword = PasswordUtil.hashPassword(password);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Registration rejected for email {} due to invalid password input", email);
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, studentId);
            statement.setString(3, email);
            statement.setString(4, hashedPassword);

            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Registration failed for email {}", email, ex);
            return false;
        }
    }

    private void migrateLegacyPassword(Connection connection, long userId, String rawPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PasswordUtil.hashPassword(rawPassword));
            statement.setLong(2, userId);
            statement.executeUpdate();
            LOGGER.info("Migrated legacy plaintext password for user id {}", userId);
        } catch (SQLException ex) {
            LOGGER.warn("Failed to migrate legacy password for user id {}", userId, ex);
        }
    }
}
