package com.example.workflow.batch;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BatchFailureOnceTracker {
    private final Set<String> failedKeys = ConcurrentHashMap.newKeySet();

    public boolean isFirstFailure(String executionKey) {
        return failedKeys.add(executionKey);
    }

    public void clear() {
        failedKeys.clear();
    }
}
