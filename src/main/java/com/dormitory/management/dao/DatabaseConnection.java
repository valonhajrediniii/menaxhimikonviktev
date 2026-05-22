package com.dormitory.management.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String URL_KEY = "db.url";
    private static final String USER_KEY = "db.user";
    private static final String PASSWORD_KEY = "db.password";

    private static final String URL_ENV = "DORMITORY_DB_URL";
    private static final String USER_ENV = "DORMITORY_DB_USER";
    private static final String PASSWORD_ENV = "DORMITORY_DB_PASSWORD";

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = value(URL_ENV, URL_KEY);
        String user = value(USER_ENV, USER_KEY);
        String password = value(PASSWORD_ENV, PASSWORD_KEY);

        if (isBlank(url) || isBlank(user)) {
            throw new IllegalStateException("Database configuration is missing. Set env vars or config/database.properties.");
        }

        return DriverManager.getConnection(url, user, Objects.requireNonNullElse(password, ""));
    }

    private static String value(String envKey, String propertyKey) {
        String envValue = System.getenv(envKey);
        if (!isBlank(envValue)) {
            return envValue;
        }
        return PROPERTIES.getProperty(propertyKey);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config/database.properties")) {
            if (input != null) {
                properties.load(input);
                return properties;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load database properties", e);
        }

        // Fallback for IDE runs where resources are not copied to the runtime classpath.
        Path[] fallbackPaths = new Path[] {
                Path.of("config", "database.properties"),
                Path.of("src", "main", "resources", "config", "database.properties")
        };

        for (Path path : fallbackPaths) {
            if (!Files.exists(path)) {
                continue;
            }
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
                return properties;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load database properties from " + path, e);
            }
        }

        return properties;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
