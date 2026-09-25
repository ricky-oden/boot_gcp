package com.example.workflow.service;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.exception.ApplicationNotFoundException;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 学習専用。既存APIからは呼ばず、テストでdirty checkingとrollbackを観察する。 */
@Service
public class ApprovalTransactionLabService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository historyRepository;

    public ApprovalTransactionLabService(ApplicationRepository applicationRepository,
                                         ApplicationHistoryRepository historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void approveUsingDirtyChecking(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
        application.approve(); // saveを呼ばなくても、commit時にUPDATEされる。
        historyRepository.save(new ApplicationHistory(application, "APPROVED_BY_LAB"));
    }
}
