package com.example.workflow.mybatis;

import com.example.workflow.exception.AlreadyApprovedException;
import com.example.workflow.exception.ApplicationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** XML Mapperを追うための学習経路。既存REST APIのJPA経路は変更しない。 */
@Service
public class MyBatisApplicationService {
    private final ApplicationMapper mapper;

    public MyBatisApplicationService(ApplicationMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public MyBatisApplicationRow create(String title) {
        MyBatisApplicationRow row = new MyBatisApplicationRow();
        row.setTitle(title.trim());
        row.setStatus("PENDING");
        row.setCreatedAt(Instant.now());
        mapper.insert(row);
        return row;
    }

    @Transactional(readOnly = true)
    public List<MyBatisApplicationRow> findPending() {
        return mapper.findByStatus("PENDING");
    }

    @Transactional
    public MyBatisApplicationRow approve(Long id) {
        mapper.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
        if (mapper.updateStatus(id, "PENDING", "APPROVED") == 0) {
            throw new AlreadyApprovedException(id);
        }
        return mapper.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }
}
