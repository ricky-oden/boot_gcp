package com.example.workflow.controller;

import com.example.workflow.dto.ApplicationResponse;
import com.example.workflow.dto.CreateApplicationRequest;
import com.example.workflow.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    // Controller自身はServiceをnewせず、Springから受け取ります。
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "申請一覧を取得", description = "作成日時の降順で申請を返します。")
    @ApiResponse(responseCode = "200", description = "取得成功")
    public List<ApplicationResponse> findAll() {
        return applicationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "申請を登録")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "登録成功"),
            @ApiResponse(responseCode = "400", description = "入力値不正",
                    content = @Content(schema = @Schema(implementation = com.example.workflow.dto.ErrorResponse.class)))
    })
    public ApplicationResponse create(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.create(request);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "申請を承認", description = "PENDINGの申請をAPPROVEDへ変更し、履歴を保存します。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "承認成功"),
            @ApiResponse(responseCode = "404", description = "申請なし"),
            @ApiResponse(responseCode = "409", description = "承認済み")
    })
    public ApplicationResponse approve(@PathVariable Long id) {
        return applicationService.approve(id);
    }
}
