package com.example.workflow.service;

import com.example.workflow.dto.ApplicationResponse;
import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationApprovalPracticeTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository historyRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void pendingApplicationCanBeApprovedPractice() {
        // Arrange: RepositoryはMock、Serviceは@InjectMocksが作る本物です。
        Application application = new Application("PC購入申請");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act
        ApplicationResponse response = applicationService.approve(1L);

        // Assert
        assertEquals(ApplicationStatus.APPROVED, response.status());
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any(ApplicationHistory.class));
    }
}
