package com.dormitory.management.controller;

import com.dormitory.management.model.Role;
import com.dormitory.management.service.AuthService;
import com.dormitory.management.service.JdbcAuthService;
import com.dormitory.management.util.AlertUtil;
import com.dormitory.management.util.SceneManager;
import com.dormitory.management.util.UserSession;
import com.dormitory.management.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final AuthService authService = new JdbcAuthService();

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private void onLogin() {
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        if (!ValidationUtil.isValidEmail(email) || !ValidationUtil.hasText(password)) {
            AlertUtil.error("Login Failed", "Please enter a valid email and password.");
            return;
        }

        authService.login(email.trim(), password)
                .ifPresentOrElse(user -> {
                    UserSession.setCurrentUser(user);
                    if (user.getRole() == Role.ADMIN) {
                        SceneManager.switchTo("admin-dashboard.fxml", "Admin Dashboard");
                    } else {
                        SceneManager.switchTo("user-dashboard.fxml", "User Dashboard");
                    }
                }, () -> AlertUtil.error("Login Failed", "Invalid email or password."));
    }

    @FXML
    private void onOpenRegister() {
        SceneManager.switchTo("register.fxml", "Create Account");
    }
}
