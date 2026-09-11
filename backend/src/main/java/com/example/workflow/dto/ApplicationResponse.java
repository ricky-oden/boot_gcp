package com.example.workflow.dto;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationStatus;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        String title,
        ApplicationStatus status,
        Instant createdAt
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getTitle(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}

