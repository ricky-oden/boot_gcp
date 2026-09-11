package com.example.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Application() {
        // JPAがEntityを復元するために必要です。
    }

    public Application(String title) {
        this.title = title;
        this.status = ApplicationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

