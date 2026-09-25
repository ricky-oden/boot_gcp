package com.example.workflow.repository;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStatusOrderByCreatedAtAsc(ApplicationStatus status);
}
