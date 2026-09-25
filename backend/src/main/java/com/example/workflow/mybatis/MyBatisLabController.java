package com.example.workflow.mybatis;

import com.example.workflow.dto.CreateApplicationRequest;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Local Dockerでのみ有効なMyBatis追跡用入口。Cloud profileではBean自体が作られない。 */
@Hidden
@Profile("local-labs")
@RestController
@RequestMapping("/labs/mybatis/applications")
public class MyBatisLabController {
    private final MyBatisApplicationService service;

    public MyBatisLabController(MyBatisApplicationService service) {
        this.service = service;
    }

    @GetMapping("/pending")
    public List<MyBatisApplicationRow> findPending() {
        return service.findPending();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MyBatisApplicationRow create(@Valid @RequestBody CreateApplicationRequest request) {
        return service.create(request.title());
    }

    @PostMapping("/{id}/approve")
    public MyBatisApplicationRow approve(@PathVariable Long id) {
        return service.approve(id);
    }
}
