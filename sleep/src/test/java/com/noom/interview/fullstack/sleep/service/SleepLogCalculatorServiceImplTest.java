package com.noom.interview.fullstack.sleep.service;


import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SleepLogCalculatorServiceImplTest {
    private final SleepLogCalculatorServiceImpl service = new SleepLogCalculatorServiceImpl();


    @Test
    void calculateAverageTimeInBed_returnsAverageMinutes() {
        SleepEntryEntity e1 = new SleepEntryEntity();
        e1.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        e1.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z")); // 480

        SleepEntryEntity e2 = new SleepEntryEntity();
        e2.setTimeInBedStart(Instant.parse("2026-02-10T23:00:00Z"));
        e2.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z")); // 420

        // average = (480 + 420) / 2 = 450.0
        int avg = service.calculateAverageTimeInBed(List.of(e1, e2));

        assertThat(avg).isEqualByComparingTo(450);
    }

    @Test
    void calculateAverageTimeInBed_returnsZero_whenListIsEmpty() {
        int avg = service.calculateAverageTimeInBed(List.of());

        assertThat(avg).isEqualByComparingTo(0);
    }

    @Test
    void calculateAverageTimeUserGetsInBed_returnsExpectedAverageTime() {
        // 22:00, 23:00, 23:00 UTC on the same date
        SleepEntryEntity e1 = new SleepEntryEntity();
        e1.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));

        SleepEntryEntity e2 = new SleepEntryEntity();
        e2.setTimeInBedStart(Instant.parse("2026-02-10T23:00:00Z"));

        SleepEntryEntity e3 = new SleepEntryEntity();
        e3.setTimeInBedStart(Instant.parse("2026-02-10T23:00:00Z"));

        LocalTime avg = service.calculateAverageTimeUserGetsInBed(List.of(e1, e2, e3));

        // average seconds: (22:00 + 23:00 + 23:00) / 3 = 22:40
        assertThat(avg).isEqualTo(LocalTime.of(22, 40));
    }

    @Test
    void calculateAverageTimeUserGetsInBed_returnsMidnight_whenEmptyList() {
        LocalTime avg = service.calculateAverageTimeUserGetsInBed(List.of());

        // because average().orElse(0.0) => Instant epoch => 00:00 UTC
        assertThat(avg).isEqualTo(LocalTime.MIDNIGHT);
    }

    @Test
    void calculateAverageTimeUserGetsOutOfBed_returnsExpectedAverageTime() {
        // 06:00, 07:00, 07:00 UTC -> average = 06:40
        SleepEntryEntity e1 = new SleepEntryEntity();
        e1.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));

        SleepEntryEntity e2 = new SleepEntryEntity();
        e2.setTimeInBedEnd(Instant.parse("2026-02-11T07:00:00Z"));

        SleepEntryEntity e3 = new SleepEntryEntity();
        e3.setTimeInBedEnd(Instant.parse("2026-02-11T07:00:00Z"));

        LocalTime avg = service.calculateAverageTimeUserGetsOutOfBed(List.of(e1, e2, e3));

        assertThat(avg).isEqualTo(LocalTime.of(6, 40));
    }

    @Test
    void calculateAverageTimeUserGetsOutOfBed_returnsMidnight_whenEmptyList() {
        LocalTime avg = service.calculateAverageTimeUserGetsOutOfBed(List.of());

        // orElse(0.0) -> epoch 0 -> 00:00 UTC
        assertThat(avg).isEqualTo(LocalTime.MIDNIGHT);
    }

    @Test
    void calculateMorningFrequencies() {
        SleepEntryEntity e1 = new SleepEntryEntity();
        e1.setMorningFeeling(MorningFeeling.GOOD);

        SleepEntryEntity e2 = new SleepEntryEntity();
        e2.setMorningFeeling(MorningFeeling.GOOD);

        SleepEntryEntity e3 = new SleepEntryEntity();
        e3.setMorningFeeling(MorningFeeling.GOOD);

        SleepEntryEntity e4 = new SleepEntryEntity();
        e4.setMorningFeeling(MorningFeeling.OK);

        SleepEntryEntity e5 = new SleepEntryEntity();
        e5.setMorningFeeling(MorningFeeling.BAD);

        SleepEntryEntity e6 = new SleepEntryEntity();
        e6.setMorningFeeling(MorningFeeling.BAD);

        Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> morningFeelingFeelingFrequencyMap = service.calculateMorningFrequencies(List.of(e1, e2, e3, e4, e5, e6));

        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.GOOD).getCount()).isEqualTo(3);
        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.GOOD).getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.OK).getCount()).isEqualTo(1);
        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.OK).getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(16.67));
        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.BAD).getCount()).isEqualTo(2);
        assertThat(morningFeelingFeelingFrequencyMap.get(MorningFeeling.BAD).getPercentage()).isEqualByComparingTo(BigDecimal.valueOf(33.33));
    }
}