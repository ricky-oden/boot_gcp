package com.example.workflow.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApplicationMapper {
    int insert(MyBatisApplicationRow application);

    Optional<MyBatisApplicationRow> findById(@Param("id") Long id);

    List<MyBatisApplicationRow> findByStatus(@Param("status") String status);

    int updateStatus(@Param("id") Long id,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("newStatus") String newStatus);
}
