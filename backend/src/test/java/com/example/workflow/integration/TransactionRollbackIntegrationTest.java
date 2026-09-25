package com.example.workflow.integration;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import com.example.workflow.service.ApprovalTransactionLabService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class TransactionRollbackIntegrationTest {
    @Autowired ApprovalTransactionLabService service;
    @Autowired ApplicationRepository applicationRepository;
    @SpyBean ApplicationHistoryRepository historyRepository;

    @BeforeEach
    void cleanDatabase() {
        historyRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void commitFlushesDirtyEntityAndHistoryTogether() {
        Application saved = applicationRepository.save(new Application("正常commit"));
        service.approveUsingDirtyChecking(saved.getId());

        assertThat(applicationRepository.findById(saved.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.APPROVED);
        assertThat(historyRepository.countByApplicationId(saved.getId())).isEqualTo(1);
    }

    @Test
    void historyFailureRollsBackDirtyCheckingUpdate() {
        Application saved = applicationRepository.save(new Application("rollback確認"));
        doThrow(new DataIntegrityViolationException("意図した履歴保存失敗"))
                .when(historyRepository).save(any(ApplicationHistory.class));

        assertThatThrownBy(() -> service.approveUsingDirtyChecking(saved.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(applicationRepository.findById(saved.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.PENDING);
        assertThat(historyRepository.countByApplicationId(saved.getId())).isZero();
    }
}
