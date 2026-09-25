package com.example.workflow.mybatis;

import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MyBatisApplicationMapperIntegrationTest {
    @Autowired MyBatisApplicationService service;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired ApplicationHistoryRepository historyRepository;

    @BeforeEach
    void cleanDatabase() {
        historyRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void xmlInsertSelectResultMapAndUpdateWorkAgainstDatabase() {
        MyBatisApplicationRow created = service.create("MyBatis申請");

        assertThat(created.getId()).isNotNull();
        assertThat(service.findPending())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.getTitle()).isEqualTo("MyBatis申請");
                    assertThat(row.getCreatedAt()).isNotNull();
                });
        assertThat(service.approve(created.getId()).getStatus()).isEqualTo("APPROVED");
        assertThat(service.findPending()).isEmpty();
    }
}
