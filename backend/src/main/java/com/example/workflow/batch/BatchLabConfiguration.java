package com.example.workflow.batch;

import com.example.workflow.entity.Application;
import com.example.workflow.entity.ApplicationHistory;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchLabConfiguration {
    public static final String JOB_NAME = "pendingApplicationApprovalJob";

    @Bean
    Job pendingApplicationJob(JobRepository jobRepository, Step pendingApplicationStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(pendingApplicationStep)
                .build();
    }

    @Bean
    Step pendingApplicationStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                IteratorItemReader<Application> pendingApplicationReader,
                                ItemProcessor<Application, Application> pendingApplicationProcessor,
                                ApplicationRepository applicationRepository,
                                ApplicationHistoryRepository historyRepository) {
        return new StepBuilder("approvePendingApplications", jobRepository)
                .<Application, Application>chunk(2, transactionManager)
                .reader(pendingApplicationReader)
                .processor(pendingApplicationProcessor)
                .writer(chunk -> {
                    for (Application application : chunk) {
                        Application saved = applicationRepository.save(application);
                        historyRepository.save(new ApplicationHistory(saved, "APPROVED_BY_BATCH"));
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    IteratorItemReader<Application> pendingApplicationReader(ApplicationRepository repository) {
        return new IteratorItemReader<>(
                repository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.PENDING));
    }

    @Bean
    @StepScope
    ItemProcessor<Application, Application> pendingApplicationProcessor(
            BatchFailureOnceTracker tracker,
            @Value("#{jobParameters['executionKey']}") String executionKey,
            @Value("#{jobParameters['failOnTitle']}") String failOnTitle) {
        return application -> {
            if (failOnTitle != null
                    && failOnTitle.equals(application.getTitle())
                    && tracker.isFirstFailure(executionKey)) {
                throw new IllegalStateException("学習用の意図したBatch失敗: " + failOnTitle);
            }
            application.approve();
            return application;
        };
    }
}
