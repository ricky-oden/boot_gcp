package com.example.workflow.dto;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message) {
}

