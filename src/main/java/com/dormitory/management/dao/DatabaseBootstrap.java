package com.dormitory.management.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBootstrap.class);
    private static final String INIT_SQL_RESOURCE = "db/init.sql";

    private DatabaseBootstrap() {
    }

    public static void initializeCoreSchema() throws SQLException {
        String sqlScript = loadInitSql();
        List<String> statements = parseStatements(sqlScript);

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
                connection.commit();
                LOGGER.info("Database schema initialized from {} with {} statements", INIT_SQL_RESOURCE, statements.size());
            } catch (SQLException ex) {
                connection.rollback();
                LOGGER.error("Database schema initialization failed. Rolled back startup transaction.", ex);
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private static String loadInitSql() {
        try (InputStream input = DatabaseBootstrap.class.getClassLoader().getResourceAsStream(INIT_SQL_RESOURCE)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read " + INIT_SQL_RESOURCE + " from classpath", ex);
        }

        Path fallbackPath = Path.of("src", "main", "resources", "db", "init.sql");
        if (Files.exists(fallbackPath)) {
            try {
                return Files.readString(fallbackPath, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read SQL file from " + fallbackPath, ex);
            }
        }

        throw new IllegalStateException("Schema file not found at classpath " + INIT_SQL_RESOURCE + " or " + fallbackPath);
    }

    private static List<String> parseStatements(String script) {
        String withoutComments = script.replaceAll("(?m)^\\s*--.*$", "");
        String[] parts = withoutComments.split(";");
        List<String> statements = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }

        if (statements.isEmpty()) {
            throw new IllegalStateException("No SQL statements found in init.sql");
        }

        return statements;
    }
}