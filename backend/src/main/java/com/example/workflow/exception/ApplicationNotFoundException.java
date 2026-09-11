package com.example.workflow.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("申請が見つかりません: id=" + id);
    }
}

