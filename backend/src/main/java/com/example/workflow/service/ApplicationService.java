package com.example.workflow.service;

import com.example.workflow.dto.ApplicationResponse;
import com.example.workflow.dto.CreateApplicationRequest;
import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.exception.AlreadyApprovedException;
import com.example.workflow.exception.ApplicationNotFoundException;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository historyRepository;

    // Constructor Injection: SpringがRepositoryの実装を外から渡します。
    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationHistoryRepository historyRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request) {
        Application saved = applicationRepository.save(new Application(request.title().trim()));
        log.info("Application created: id={}, title={}", saved.getId(), saved.getTitle());
        return ApplicationResponse.from(saved);
    }

    @Transactional // 状態更新と履歴保存を、成功も失敗も一緒の1単位にします。
    public ApplicationResponse approve(Long id) {
        log.info("Approval started: applicationId={}", id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Approval failed: application not found, id={}", id);
                    return new ApplicationNotFoundException(id);
                });

        if (application.getStatus() == ApplicationStatus.APPROVED) {
            log.error("Approval failed: already approved, id={}", id);
            throw new AlreadyApprovedException(id);
        }

        application.approve();
        applicationRepository.save(application);
        historyRepository.save(new ApplicationHistory(application, "APPROVED"));
        log.info("Approval succeeded: applicationId={}", id);

        return ApplicationResponse.from(application);
    }
}

