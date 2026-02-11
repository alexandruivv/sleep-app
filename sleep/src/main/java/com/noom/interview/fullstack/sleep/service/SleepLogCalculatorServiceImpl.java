package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SleepLogCalculatorServiceImpl implements SleepLogCalculatorService {
    @Override
    public int calculateAverageTimeInBed(List<SleepEntryEntity> sleepEntryEntities) {
        return (int) sleepEntryEntities.stream()
                .mapToLong(sleepEntry -> Duration.between(sleepEntry.getTimeInBedStart(), sleepEntry.getTimeInBedEnd()).toMinutes())
                .average()
                .orElse(0);
    }

    @Override
    public LocalTime calculateAverageTimeUserGetsInBed(List<SleepEntryEntity> sleepEntryEntities) {
        long avgEpochSeconds = Math.round(
                sleepEntryEntities.stream()
                        .mapToLong(se -> se.getTimeInBedStart().getEpochSecond())
                        .average()
                        .orElse(0.0)
        );

        return Instant.ofEpochSecond(avgEpochSeconds)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
    }

    @Override
    public LocalTime calculateAverageTimeUserGetsOutOfBed(List<SleepEntryEntity> sleepEntryEntities) {
        long avgEpochSeconds = Math.round(
                sleepEntryEntities.stream()
                        .mapToLong(se -> se.getTimeInBedEnd().getEpochSecond())
                        .average()
                        .orElse(0.0)
        );

        return Instant.ofEpochSecond(avgEpochSeconds)
                .atZone(ZoneOffset.UTC)
                .toLocalTime();
    }

    @Override
    public Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> calculateMorningFrequencies(List<SleepEntryEntity> sleepEntryEntities) {
        Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> frequencies = new EnumMap<>(MorningFeeling.class);
        for (SleepEntryEntity entry : sleepEntryEntities) {
            MorningFeeling currentMorningFeeling = entry.getMorningFeeling();
            frequencies.putIfAbsent(
                    currentMorningFeeling,
                    SleepLogAveragesResponse.FeelingFrequency.builder()
                            .count(0)
                            .percentage(BigDecimal.ZERO)
                            .build()
            );
            SleepLogAveragesResponse.FeelingFrequency currentFrequency = frequencies.get(currentMorningFeeling);
            int newCount = currentFrequency.getCount() + 1;
            SleepLogAveragesResponse.FeelingFrequency newFrequency = SleepLogAveragesResponse.FeelingFrequency.builder()
                    .count(newCount)
                    .percentage(BigDecimal.valueOf((double) newCount / sleepEntryEntities.size() * 100.0).setScale(2, RoundingMode.HALF_UP))
                    .build();
            frequencies.put(currentMorningFeeling, newFrequency);
        }
        return frequencies;
    }
}
