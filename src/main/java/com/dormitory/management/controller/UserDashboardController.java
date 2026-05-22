package com.dormitory.management.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.dormitory.management.model.Application;
import com.dormitory.management.model.ApplicationStatus;
import com.dormitory.management.model.StudentProfile;
import com.dormitory.management.model.User;
import com.dormitory.management.service.ApplicationService;
import com.dormitory.management.service.ComplaintService;
import com.dormitory.management.service.StudentProfileService;
import com.dormitory.management.util.AlertUtil;
import com.dormitory.management.util.OperationResult;
import com.dormitory.management.util.SceneManager;
import com.dormitory.management.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserDashboardController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DUMMY_QR_PAYLOAD = "DORMITORY_ACCEPTED_DEMO_2026";
        private static final List<String> STATUS_STYLE_CLASSES = List.of(
            "status-pill-pending",
            "status-pill-accepted",
            "status-pill-rejected",
            "status-pill-neutral"
        );

    private final StudentProfileService studentProfileService = new StudentProfileService();
    private final ApplicationService applicationService = new ApplicationService();
    private final ComplaintService complaintService = new ComplaintService();

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileStudentIdLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextField facultyField;

    @FXML
    private TextField studyProgramField;

    @FXML
    private TextField yearOfStudyField;

    @FXML
    private TextField genderField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField photoUrlField;

    @FXML
    private TabPane dashboardTabPane;

    @FXML
    private Tab applicationTab;

    @FXML
    private TextArea applicationNotesArea;

    @FXML
    private TextArea complaintMessageArea;

    @FXML
    private Label applicationStatusLabel;

    @FXML
    private Label applicationDateLabel;

    @FXML
    private Label dormitoryLabel;

    @FXML
    private Label roomLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private ImageView qrImageView;

    @FXML
    private Label qrHintLabel;

    @FXML
    private void initialize() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            AlertUtil.error("Session Expired", "Please login again.");
            SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
            return;
        }

        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        profileNameLabel.setText(currentUser.getFullName());
        profileStudentIdLabel.setText(currentUser.getStudentId() == null ? "-" : currentUser.getStudentId());
        profileImageView.setImage(createPlaceholderAvatar());
        loadProfile(currentUser.getId());
        loadApplicationStatus(currentUser.getId());
    }

    @FXML
    private void onSaveProfile() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            AlertUtil.error("Session Expired", "Please login again.");
            return;
        }

        OperationResult result = saveProfileForUser(user);
        if (result.isSuccess()) {
            AlertUtil.info("Profile", result.getMessage());
        } else {
            AlertUtil.error("Profile", result.getMessage());
        }
    }

    @FXML
    private void onPreviewPhoto() {
        updateProfileImage(photoUrlField.getText());
    }

    @FXML
    private void onChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp")
        );

        String currentPath = photoUrlField.getText();
        if (currentPath != null && !currentPath.isBlank()) {
            File selected = new File(currentPath.trim());
            File parent = selected.isDirectory() ? selected : selected.getParentFile();
            if (parent != null && parent.exists()) {
                chooser.setInitialDirectory(parent);
            }
        }

        File file = chooser.showOpenDialog(photoUrlField.getScene().getWindow());
        if (file == null) {
            return;
        }

        photoUrlField.setText(file.getAbsolutePath());
        updateProfileImage(file.getAbsolutePath());
    }

    @FXML
    private void onSubmitApplication() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            AlertUtil.error("Session Expired", "Please login again.");
            return;
        }

        String notes = applicationNotesArea.getText();
        if (notes != null && notes.length() > 1000) {
            AlertUtil.error("Validation Error", "Application notes can have up to 1000 characters.");
            return;
        }

        if (studentProfileService.getByUserId(user.getId()).isEmpty()) {
            OperationResult saveResult = saveProfileForUser(user);
            if (!saveResult.isSuccess()) {
                AlertUtil.error("Application", "Please complete and save your profile before applying. " + saveResult.getMessage());
                dashboardTabPane.getSelectionModel().select(0);
                return;
            }
            AlertUtil.info("Profile", "Profile saved. You can now submit your application.");
        }

        OperationResult result = applicationService.submitApplication(user.getId(), notes);
        if (result.isSuccess()) {
            AlertUtil.info("Application", result.getMessage());
            loadApplicationStatus(user.getId());
        } else {
            AlertUtil.error("Application", result.getMessage());
        }
    }

    @FXML
    private void onRefreshStatus() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            loadApplicationStatus(user.getId());
        }
    }

    @FXML
    private void onSubmitComplaint() {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            AlertUtil.error("Session Expired", "Please login again.");
            return;
        }

        OperationResult result = complaintService.submitComplaint(user.getId(), complaintMessageArea.getText());
        if (result.isSuccess()) {
            AlertUtil.info("Complaint", result.getMessage());
            complaintMessageArea.clear();
        } else {
            AlertUtil.error("Complaint", result.getMessage());
        }
    }

    @FXML
    private void onLogout() {
        UserSession.clear();
        SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
    }

    private void loadProfile(long userId) {
        studentProfileService.getByUserId(userId).ifPresent(profile -> {
            facultyField.setText(profile.getFaculty());
            studyProgramField.setText(profile.getStudyProgram());
            yearOfStudyField.setText(String.valueOf(profile.getYearOfStudy()));
            genderField.setText(profile.getGender());
            phoneField.setText(profile.getPhone());
            cityField.setText(profile.getCity());
            photoUrlField.setText(profile.getPhotoUrl());
            updateProfileImage(profile.getPhotoUrl());
        });
    }

    private OperationResult saveProfileForUser(User user) {
        int yearOfStudy;
        try {
            yearOfStudy = Integer.parseInt(yearOfStudyField.getText().trim());
        } catch (NumberFormatException ex) {
            return OperationResult.failure("Year of study must be a number between 1 and 6.");
        }

        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setFaculty(facultyField.getText() == null ? "" : facultyField.getText().trim());
        profile.setStudyProgram(studyProgramField.getText() == null ? "" : studyProgramField.getText().trim());
        profile.setYearOfStudy(yearOfStudy);
        profile.setGender(genderField.getText() == null ? "" : genderField.getText().trim());
        profile.setPhone(phoneField.getText() == null ? "" : phoneField.getText().trim());
        profile.setCity(cityField.getText() == null ? "" : cityField.getText().trim());
        profile.setPhotoUrl(photoUrlField.getText() == null ? "" : photoUrlField.getText().trim());

        OperationResult result = studentProfileService.saveProfile(profile);
        if (result.isSuccess()) {
            updateProfileImage(profile.getPhotoUrl());
        }
        return result;
    }

    private void loadApplicationStatus(long userId) {
        Application application = applicationService.getByUserId(userId).orElse(null);
        if (application == null) {
            applicationStatusLabel.setText("No application submitted yet.");
            updateStatusPillStyle(null);
            applicationDateLabel.setText("-");
            dormitoryLabel.setText("-");
            roomLabel.setText("-");
            statusMessageLabel.setText("Create your application when your profile is complete.");
            showApplicationTab(true);
            hideQr();
            return;
        }

        applicationStatusLabel.setText(application.getStatus().name());
    updateStatusPillStyle(application.getStatus());
        applicationDateLabel.setText(application.getApplicationDate() == null
                ? "-"
                : application.getApplicationDate().format(DATE_TIME_FORMATTER));

        if (application.getDormitoryNumber() != null && application.getRoomNumber() != null) {
            dormitoryLabel.setText(application.getDormitoryNumber());
            roomLabel.setText(application.getRoomNumber());
        } else {
            dormitoryLabel.setText("Not assigned yet");
            roomLabel.setText("Not assigned yet");
        }

        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            statusMessageLabel.setText("Accepted. Show the QR code for verification.");
            showApplicationTab(false);
            showQr();
        } else if (application.getStatus() == ApplicationStatus.REJECTED) {
            statusMessageLabel.setText("Rejected. You can apply again.");
            showApplicationTab(true);
            hideQr();
        } else {
            statusMessageLabel.setText("Pending review by the admin.");
            showApplicationTab(false);
            hideQr();
        }
    }

    private void updateStatusPillStyle(ApplicationStatus status) {
        applicationStatusLabel.getStyleClass().removeAll(STATUS_STYLE_CLASSES);

        if (status == null) {
            applicationStatusLabel.getStyleClass().add("status-pill-neutral");
            return;
        }

        switch (status) {
            case ACCEPTED -> applicationStatusLabel.getStyleClass().add("status-pill-accepted");
            case REJECTED -> applicationStatusLabel.getStyleClass().add("status-pill-rejected");
            case PENDING -> applicationStatusLabel.getStyleClass().add("status-pill-pending");
            default -> applicationStatusLabel.getStyleClass().add("status-pill-neutral");
        }
    }

    private void showApplicationTab(boolean show) {
        boolean contains = dashboardTabPane.getTabs().contains(applicationTab);
        if (show && !contains) {
            dashboardTabPane.getTabs().add(1, applicationTab);
        }
        if (!show && contains) {
            dashboardTabPane.getTabs().remove(applicationTab);
        }
    }

    private void showQr() {
        qrImageView.setImage(createQrImage(DUMMY_QR_PAYLOAD, 200, 200));
        qrImageView.setVisible(true);
        qrImageView.setManaged(true);
        qrHintLabel.setText("Dummy verification QR (same data for all students)");
    }

    private void hideQr() {
        qrImageView.setImage(null);
        qrImageView.setVisible(false);
        qrImageView.setManaged(false);
        qrHintLabel.setText("QR code appears after acceptance.");
    }

    private WritableImage createQrImage(String payload, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, width, height);
            WritableImage image = new WritableImage(width, height);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.getPixelWriter().setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return image;
        } catch (WriterException ex) {
            return createPlaceholderAvatar();
        }
    }

    private void updateProfileImage(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            profileImageView.setImage(createPlaceholderAvatar());
            return;
        }

        try {
            String source = photoUrl.trim();
            if (!source.startsWith("http://") && !source.startsWith("https://") && !source.startsWith("file:")) {
                source = new File(source).toURI().toString();
            }

            Image image = new Image(source, true);
            if (image.isError()) {
                profileImageView.setImage(createPlaceholderAvatar());
            } else {
                profileImageView.setImage(image);
            }
        } catch (RuntimeException ex) {
            profileImageView.setImage(createPlaceholderAvatar());
        }
    }

    private WritableImage createPlaceholderAvatar() {
        int size = 140;
        WritableImage image = new WritableImage(size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x < 6 || y < 6 || x >= size - 6 || y >= size - 6) {
                    image.getPixelWriter().setColor(x, y, Color.web("#1e4d8a"));
                } else {
                    image.getPixelWriter().setColor(x, y, Color.web("#dbe8ff"));
                }
            }
        }
        return image;
    }
}
