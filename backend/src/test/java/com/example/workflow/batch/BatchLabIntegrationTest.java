package com.example.workflow.batch;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BatchLabIntegrationTest {
    @Autowired JobLauncher jobLauncher;
    @Autowired Job pendingApplicationJob;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired ApplicationHistoryRepository historyRepository;
    @Autowired BatchFailureOnceTracker failureTracker;

    @BeforeEach
    void cleanBusinessData() {
        historyRepository.deleteAll();
        applicationRepository.deleteAll();
        failureTracker.clear();
    }

    @Test
    void successfulJobProcessesPendingItemsInChunks() throws Exception {
        applicationRepository.save(new Application("batch-1"));
        applicationRepository.save(new Application("batch-2"));
        JobParameters parameters = parameters("success-" + System.nanoTime(), null);

        JobExecution execution = jobLauncher.run(pendingApplicationJob, parameters);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(applicationRepository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING)).isEmpty();
        assertThat(historyRepository.count()).isEqualTo(2);
    }

    @Test
    void failedJobCanRestartWithSameParametersAndIsIdempotent() throws Exception {
        applicationRepository.save(new Application("before-failure-1"));
        applicationRepository.save(new Application("before-failure-2"));
        applicationRepository.save(new Application("fail-once"));
        JobParameters parameters = parameters("restart-" + System.nanoTime(), "fail-once");

        JobExecution failed = jobLauncher.run(pendingApplicationJob, parameters);
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);

        JobExecution restarted = jobLauncher.run(pendingApplicationJob, parameters);
        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(applicationRepository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING)).isEmpty();
        assertThat(historyRepository.count()).isEqualTo(3);

        assertThatThrownBy(() -> jobLauncher.run(pendingApplicationJob, parameters))
                .isInstanceOf(JobInstanceAlreadyCompleteException.class);

        JobExecution newInstance = jobLauncher.run(
                pendingApplicationJob, parameters("idempotent-" + System.nanoTime(), null));
        assertThat(newInstance.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(historyRepository.count()).isEqualTo(3);
    }

    private JobParameters parameters(String executionKey, String failOnTitle) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("executionKey", executionKey);
        if (failOnTitle != null) {
            builder.addString("failOnTitle", failOnTitle);
        }
        return builder.toJobParameters();
    }
}
