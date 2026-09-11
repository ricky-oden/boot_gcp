package com.example.workflow.exception;

public class AlreadyApprovedException extends RuntimeException {

    public AlreadyApprovedException(Long id) {
        super("既に承認済みの申請です: id=" + id);
    }
}

