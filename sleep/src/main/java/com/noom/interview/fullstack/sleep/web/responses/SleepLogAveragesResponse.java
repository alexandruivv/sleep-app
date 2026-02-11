package com.noom.interview.fullstack.sleep.web.responses;


import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Value
@Builder
public class SleepLogAveragesResponse {
    LocalDate rangeStart;
    LocalDate rangeEnd;
    int averageTimeInBedMinutes;
    LocalTime averageTimeUserGetsInBed;
    LocalTime averageTimeUserGetsOutOfBed;
    Map<MorningFeeling, FeelingFrequency> morningFeelingFrequencies;

    @Value
    @Builder
    public static class FeelingFrequency {
        int count;
        BigDecimal percentage;
    }
}
