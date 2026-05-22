package com.dormitory.management.controller;

import com.dormitory.management.service.AuthService;
import com.dormitory.management.service.JdbcAuthService;
import com.dormitory.management.util.AlertUtil;
import com.dormitory.management.util.SceneManager;
import com.dormitory.management.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    private final AuthService authService = new JdbcAuthService();

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onRegister() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String studentId = studentIdField.getText();
        String password = passwordField.getText();

        if (!ValidationUtil.hasText(fullName)
                || !ValidationUtil.hasText(studentId)
                || !ValidationUtil.isValidEmail(email)
                || !ValidationUtil.minLength(password, 6)) {
            AlertUtil.error("Register Failed", "Enter valid full name, student ID, email, and password (min 6). ");
            return;
        }

        boolean registered = authService.register(fullName.trim(), studentId.trim(), email.trim(), password);
        if (registered) {
            AlertUtil.info("Register Success", "Account created as USER. Please login.");
            SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
        } else {
            AlertUtil.error("Register Failed", "Email/student ID already exists or database is unavailable.");
        }
    }

    @FXML
    private void onBackToLogin() {
        SceneManager.switchTo("login.fxml", "Dormitory Management System - Login");
    }
}
