package com.example.workflow.controller;

import com.example.workflow.dto.ApplicationResponse;
import com.example.workflow.entity.ApplicationStatus;
import com.example.workflow.exception.AlreadyApprovedException;
import com.example.workflow.exception.ApplicationNotFoundException;
import com.example.workflow.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ApplicationService applicationService;

    private final ApplicationResponse pending = new ApplicationResponse(
            1L, "PC購入申請", ApplicationStatus.PENDING, Instant.parse("2026-09-09T00:00:00Z"));

    @Test
    void getReturns200AndJsonArray() throws Exception {
        when(applicationService.findAll()).thenReturn(List.of(pending));
        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void postReturns201() throws Exception {
        when(applicationService.create(any())).thenReturn(pending);
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"PC購入申請\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("PC購入申請"));
    }

    @Test
    void blankTitleReturns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("titleは必須です"));
    }

    @Test
    void approveReturns200() throws Exception {
        ApplicationResponse approved = new ApplicationResponse(
                1L, "PC購入申請", ApplicationStatus.APPROVED, pending.createdAt());
        when(applicationService.approve(1L)).thenReturn(approved);
        mockMvc.perform(post("/applications/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void missingApplicationReturns404() throws Exception {
        when(applicationService.approve(99L)).thenThrow(new ApplicationNotFoundException(99L));
        mockMvc.perform(post("/applications/99/approve"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void doubleApprovalReturns409() throws Exception {
        when(applicationService.approve(1L)).thenThrow(new AlreadyApprovedException(1L));
        mockMvc.perform(post("/applications/1/approve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void unexpectedFailureReturnsSafe500() throws Exception {
        when(applicationService.findAll()).thenThrow(new IllegalStateException("secret detail"));
        mockMvc.perform(get("/applications"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("サーバー内部でエラーが発生しました"));
    }
}
