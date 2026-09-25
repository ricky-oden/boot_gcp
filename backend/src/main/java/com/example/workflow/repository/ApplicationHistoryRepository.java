package com.example.workflow.repository;

import com.example.workflow.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {
    List<ApplicationHistory> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    long countByApplicationId(Long applicationId);
}
