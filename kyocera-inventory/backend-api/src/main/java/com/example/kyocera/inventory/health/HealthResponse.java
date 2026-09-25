package com.example.kyocera.inventory.health;

public class HealthResponse {

    private final String status;
    private final String database;

    public HealthResponse(String status, String database) {
        this.status = status;
        this.database = database;
    }

    public String getStatus() {
        return status;
    }

    public String getDatabase() {
        return database;
    }
}
