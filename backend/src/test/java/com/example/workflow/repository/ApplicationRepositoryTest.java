package com.example.workflow.repository;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ApplicationRepositoryTest {
    @Autowired ApplicationRepository applicationRepository;
    @Autowired ApplicationHistoryRepository historyRepository;

    @Test
    void saveFindAndStatusQueryUseJpaMapping() {
        Application pending = applicationRepository.save(new Application("PC購入申請"));
        Application approved = new Application("出張申請");
        approved.approve();
        applicationRepository.save(approved);

        assertThat(applicationRepository.findById(pending.getId()))
                .get().extracting(Application::getTitle).isEqualTo("PC購入申請");
        assertThat(applicationRepository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING))
                .extracting(Application::getTitle).containsExactly("PC購入申請");
    }

    @Test
    void historyManyToOneMappingCanBeReadByApplicationId() {
        Application application = applicationRepository.save(new Application("休暇申請"));
        historyRepository.saveAndFlush(new ApplicationHistory(application, "CREATED"));

        assertThat(historyRepository.findByApplicationIdOrderByCreatedAtAsc(application.getId()))
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getAction()).isEqualTo("CREATED");
                    assertThat(history.getApplication().getId()).isEqualTo(application.getId());
                });
    }
}
