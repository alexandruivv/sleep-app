package com.noom.interview.fullstack.sleep.web.responses;

import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class SleepLogResponse {
    UUID id;
    LocalDate sleepDate;
    Instant timeInBedStart;
    Instant timeInBedEnd;
    int totalTimeInBedMinutes;
    MorningFeeling morningFeeling;
}
