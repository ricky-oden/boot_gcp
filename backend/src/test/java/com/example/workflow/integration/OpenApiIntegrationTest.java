package com.example.workflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void apiDocumentContainsThreeEndpointsAndResponseStatuses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Workflow API"))
                .andExpect(jsonPath("$.paths['/applications'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/applications'].post.requestBody").exists())
                .andExpect(jsonPath("$.paths['/applications'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/applications/{id}/approve'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/applications/{id}/approve'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/applications/{id}/approve'].post.responses['409']").exists());
    }
}
