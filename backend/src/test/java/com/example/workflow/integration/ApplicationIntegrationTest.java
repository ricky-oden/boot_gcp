package com.example.workflow.integration;

import com.example.workflow.repository.ApplicationHistoryRepository;
import com.example.workflow.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired ApplicationHistoryRepository historyRepository;

    @BeforeEach
    void cleanDatabase() {
        historyRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void controllerToDatabaseRegistrationApprovalAndHistory() throws Exception {
        String body = mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"統合テスト申請\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(body.replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));

        mockMvc.perform(post("/applications/{id}/approve", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("統合テスト申請"));
        assertThat(historyRepository.countByApplicationId(id)).isEqualTo(1);
    }

    @Test
    void secondApprovalIs409AndDoesNotAddHistory() throws Exception {
        String body = mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"二重承認確認\"}"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(body.replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));
        mockMvc.perform(post("/applications/{id}/approve", id)).andExpect(status().isOk());
        mockMvc.perform(post("/applications/{id}/approve", id)).andExpect(status().isConflict());
        assertThat(historyRepository.countByApplicationId(id)).isEqualTo(1);
    }
}
