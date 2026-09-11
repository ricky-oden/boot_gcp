package com.example.workflow.service;

import com.example.workflow.dto.ApplicationResponse;
import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.exception.AlreadyApprovedException;
import com.example.workflow.exception.ApplicationNotFoundException;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    // @Mock: DBへ接続しない偽物のRepositoryをMockitoが作ります。
    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationHistoryRepository historyRepository;

    // @InjectMocks: 上のMockを本物のServiceのConstructorへ渡します。
    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void pendingApplicationCanBeApproved() {
        // Arrange
        Application application = new Application("PC購入申請");
        // when().thenReturn(): Mockの検索結果を準備します。
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act
        ApplicationResponse response = applicationService.approve(1L);

        // Assert
        assertEquals(ApplicationStatus.APPROVED, response.status());
        // verify(): Repositoryが実際に呼ばれたことを確認します。
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any(ApplicationHistory.class));
    }

    @Test
    void missingApplicationCannotBeApproved() {
        // Arrange
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ApplicationNotFoundException exception = assertThrows(
                ApplicationNotFoundException.class,
                () -> applicationService.approve(99L)
        );

        // Assert
        assertEquals("申請が見つかりません: id=99", exception.getMessage());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void approvedApplicationCannotBeApprovedAgain() {
        // Arrange
        Application application = new Application("出張申請");
        application.approve();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act
        AlreadyApprovedException exception = assertThrows(
                AlreadyApprovedException.class,
                () -> applicationService.approve(1L)
        );

        // Assert
        assertEquals("既に承認済みの申請です: id=1", exception.getMessage());
        verify(applicationRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}

