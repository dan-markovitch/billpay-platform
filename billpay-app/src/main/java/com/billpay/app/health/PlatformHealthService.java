package com.billpay.app.health;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@Service
public class PlatformHealthService {

    private final DataSource dataSource;

    public PlatformHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, String> health() {
        String databaseStatus = checkDatabase();
        String overallStatus = "UP".equals(databaseStatus) ? "UP" : "DOWN";

        return Map.of(
            "status", overallStatus,
            "service", "billpay-platform",
            "database", databaseStatus
        );
    }

    private String checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
