package com.dormitory.management.dao;

import com.dormitory.management.model.Room;
import com.dormitory.management.model.RoomStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomDao.class);

    public List<Room> findAssignableRooms() {
        String sql = "SELECT r.id, r.dormitory_id, r.room_number, r.capacity, r.occupied_beds, r.status, "
            + "d.dorm_name "
            + "FROM rooms r "
            + "JOIN dormitories d ON d.id = r.dormitory_id "
            + "WHERE r.occupied_beds < r.capacity "
            + "ORDER BY d.dorm_name, r.room_number";

        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                rooms.add(map(rs));
            }
        } catch (SQLException | IllegalStateException ex) {
            LOGGER.error("Failed loading assignable rooms", ex);
        }

        return rooms;
    }

    public Optional<Room> findByIdForUpdate(Connection connection, long roomId) throws SQLException {
        String sql = "SELECT r.id, r.dormitory_id, r.room_number, r.capacity, r.occupied_beds, r.status, "
            + "d.dorm_name "
            + "FROM rooms r "
            + "JOIN dormitories d ON d.id = r.dormitory_id "
            + "WHERE r.id = ? "
            + "FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateOccupancy(Connection connection, long roomId, int occupiedBeds, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET occupied_beds = ?, status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, occupiedBeds);
            statement.setString(2, status.name());
            statement.setLong(3, roomId);
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Room update failed for room id " + roomId);
            }
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setDormitoryId(rs.getLong("dormitory_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setCapacity(rs.getInt("capacity"));
        room.setOccupiedBeds(rs.getInt("occupied_beds"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        room.setDormName(rs.getString("dorm_name"));
        return room;
    }
}
