package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;

import java.util.UUID;

public interface SleepLogService {
    SleepLogResponse createLastNightLog(UUID userId, CreateSleepLogRequest request);

    SleepLogResponse getLastNightLog(UUID userId);

    SleepLogAveragesResponse getLast30DayAverages(UUID userId);
}

