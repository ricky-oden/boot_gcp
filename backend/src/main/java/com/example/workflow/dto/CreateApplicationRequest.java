package com.example.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateApplicationRequest(
        @NotBlank(message = "titleは必須です") String title
) {
}

