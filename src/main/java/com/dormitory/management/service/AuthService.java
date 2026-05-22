package com.dormitory.management.service;

import com.dormitory.management.model.User;

import java.util.Optional;

public interface AuthService {
    Optional<User> login(String email, String password);

    boolean register(String fullName, String studentId, String email, String password);
}
