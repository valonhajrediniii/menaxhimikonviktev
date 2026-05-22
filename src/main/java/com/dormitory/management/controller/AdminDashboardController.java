package com.dormitory.management.controller;

import com.dormitory.management.model.AcceptedStudentView;
import com.dormitory.management.model.AdminComplaintView;
import com.dormitory.management.model.PendingApplicationView;
import com.dormitory.management.model.User;
import com.dormitory.management.service.AdminService;
import com.dormitory.management.util.AlertUtil;
import com.dormitory.management.util.OperationResult;
import com.dormitory.management.util.SceneManager;
import com.dormitory.management.util.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AdminService adminService = new AdminService();

    @FXML
    private Label adminWelcomeLabel;

    @FXML
    private TableView<PendingApplicationView> applicationsTable;

    @FXML
    private TableColumn<PendingApplicationView, String> studentNameColumn;

    @FXML
    private TableColumn<PendingApplicationView, String> studentEmailColumn;

    @FXML
    private TableColumn<PendingApplicationView, String> facultyColumn;

    @FXML
    private TableColumn<PendingApplicationView, Integer> yearColumn;

    @FXML
    private TableColumn<PendingApplicationView, String> dateColumn;

    @FXML
    private TableView<AcceptedStudentView> acceptedStudentsTable;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedStudentNameColumn;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedStudentEmailColumn;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedFacultyColumn;

    @FXML
    private TableColumn<AcceptedStudentView, Integer> acceptedYearColumn;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedDormitoryColumn;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedRoomColumn;

    @FXML
    private TableColumn<AcceptedStudentView, String> acceptedDateColumn;

    @FXML
    private ComboBox<RoomOption> roomSelector;

    @FXML
    private Label roomDetailsLabel;

    @FXML
    private TableView<AdminComplaintView> complaintsTable;

    @FXML
    private TableColumn<AdminComplaintView, String> complaintDormitoryColumn;

    @FXML
    private TableColumn<AdminComplaintView, String> complaintMessageColumn;

    @FXML
    private TableColumn<AdminComplaintView, String> complaintDateColumn;

    @FXML
    private void initialize() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            AlertUtil.error("Session Expired", "Please login again.");
            SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
            return;
        }

        adminWelcomeLabel.setText("Admin: " + user.getFullName());
        configurePendingTable();
        configureAcceptedTable();
        configureComplaintsTable();
        loadPendingApplications();
        loadAcceptedStudents();
        loadComplaints();
        loadRooms();

        roomSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                roomDetailsLabel.setText("Select a room to view details.");
                return;
            }
            roomDetailsLabel.setText("Dormitory: " + newValue.dormName
                    + " | Room: " + newValue.roomNumber
                    + " | Capacity: " + newValue.occupiedBeds + "/" + newValue.capacity);
        });
    }

    @FXML
    private void onApproveSelected() {
        PendingApplicationView selected = applicationsTable.getSelectionModel().getSelectedItem();
        RoomOption room = roomSelector.getValue();

        if (selected == null) {
            AlertUtil.error("Approve", "Select an application first.");
            return;
        }

        if (room == null) {
            AlertUtil.error("Approve", "Select a room before approving.");
            return;
        }

        OperationResult result = adminService.approveApplication(selected.getApplicationId(), room.roomId);
        if (result.isSuccess()) {
            AlertUtil.info("Approve", result.getMessage());
            refreshAll();
        } else {
            AlertUtil.error("Approve", result.getMessage());
        }
    }

    @FXML
    private void onRejectSelected() {
        PendingApplicationView selected = applicationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.error("Reject", "Select an application first.");
            return;
        }

        OperationResult result = adminService.rejectApplication(selected.getApplicationId());
        if (result.isSuccess()) {
            AlertUtil.info("Reject", result.getMessage());
            refreshAll();
        } else {
            AlertUtil.error("Reject", result.getMessage());
        }
    }

    @FXML
    private void onRefreshData() {
        refreshAll();
    }

    @FXML
    private void onLogout() {
        UserSession.clear();
        SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
    }

    private void configurePendingTable() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("studentEmail"));
        facultyColumn.setCellValueFactory(new PropertyValueFactory<>("faculty"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearOfStudy"));
        dateColumn.setCellValueFactory(cellData -> {
            String value = cellData.getValue().getApplicationDate() == null
                    ? "-"
                    : cellData.getValue().getApplicationDate().format(DATE_TIME_FORMATTER);
            return new javafx.beans.property.SimpleStringProperty(value);
        });
    }

    private void configureAcceptedTable() {
        acceptedStudentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        acceptedStudentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("studentEmail"));
        acceptedFacultyColumn.setCellValueFactory(new PropertyValueFactory<>("faculty"));
        acceptedYearColumn.setCellValueFactory(new PropertyValueFactory<>("yearOfStudy"));
        acceptedDormitoryColumn.setCellValueFactory(new PropertyValueFactory<>("dormitoryNumber"));
        acceptedRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        acceptedDateColumn.setCellValueFactory(cellData -> {
            String value = cellData.getValue().getApplicationDate() == null
                    ? "-"
                    : cellData.getValue().getApplicationDate().format(DATE_TIME_FORMATTER);
            return new javafx.beans.property.SimpleStringProperty(value);
        });
    }

    private void loadPendingApplications() {
        List<PendingApplicationView> rows = adminService.getPendingApplications();
        applicationsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void loadAcceptedStudents() {
        List<AcceptedStudentView> rows = adminService.getAcceptedStudents();
        acceptedStudentsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void loadRooms() {
        List<RoomOption> options = adminService.getAssignableRooms().stream()
                .map(room -> new RoomOption(
                        room.getId(),
                        room.getDormName(),
                        room.getRoomNumber(),
                        room.getCapacity(),
                        room.getOccupiedBeds()))
                .toList();
        roomSelector.setItems(FXCollections.observableArrayList(options));
        roomSelector.getSelectionModel().clearSelection();
        roomDetailsLabel.setText("Select a room to view details.");
    }

    private void configureComplaintsTable() {
        complaintDormitoryColumn.setCellValueFactory(cellData -> {
            String dormitory = cellData.getValue().getDormitoryNumber();
            return new javafx.beans.property.SimpleStringProperty(
                    dormitory == null || dormitory.isBlank() ? "Not assigned" : dormitory);
        });
        complaintMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        complaintDateColumn.setCellValueFactory(cellData -> {
            String value = cellData.getValue().getCreatedAt() == null
                    ? "-"
                    : cellData.getValue().getCreatedAt().format(DATE_TIME_FORMATTER);
            return new javafx.beans.property.SimpleStringProperty(value);
        });
    }

    private void loadComplaints() {
        List<AdminComplaintView> rows = adminService.getComplaintsForAdmin();
        complaintsTable.setItems(FXCollections.observableArrayList(rows));
    }

    private void refreshAll() {
        loadPendingApplications();
        loadAcceptedStudents();
        loadComplaints();
        loadRooms();
    }

    private static class RoomOption {
        private final Long roomId;
        private final String dormName;
        private final String roomNumber;
        private final int capacity;
        private final int occupiedBeds;

        private RoomOption(Long roomId, String dormName, String roomNumber, int capacity, int occupiedBeds) {
            this.roomId = roomId;
            this.dormName = dormName;
            this.roomNumber = roomNumber;
            this.capacity = capacity;
            this.occupiedBeds = occupiedBeds;
        }

        @Override
        public String toString() {
            return dormName + " - Room " + roomNumber + " (" + occupiedBeds + "/" + capacity + ")";
        }
    }
}
