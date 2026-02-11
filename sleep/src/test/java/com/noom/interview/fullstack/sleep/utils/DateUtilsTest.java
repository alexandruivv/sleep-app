package com.noom.interview.fullstack.sleep.utils;

import com.noom.interview.fullstack.sleep.validator.SleepIntervalType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void shouldReturnTrueWhenInstantIsNull() {
        assertTrue(DateUtils.validateInstantForInterval(null, SleepIntervalType.START));
        assertTrue(DateUtils.validateInstantForInterval(null, SleepIntervalType.END));
    }

    @Test
    void endInterval_futureInstant_shouldBeInvalid() {
        Instant future = Instant.now().plusSeconds(60);

        boolean result = DateUtils.validateInstantForInterval(future, SleepIntervalType.END);

        assertFalse(result);
    }

    @Test
    void endInterval_nowInstant_shouldBeValid() {
        Instant now = Instant.now();

        boolean result = DateUtils.validateInstantForInterval(now, SleepIntervalType.END);

        assertTrue(result);
    }

    @Test
    void endInterval_pastInstant_shouldBeValid() {
        Instant past = Instant.now().minusSeconds(3600);

        boolean result = DateUtils.validateInstantForInterval(past, SleepIntervalType.END);

        assertTrue(result);
    }

    @Test
    void startInterval_beforeStartOfYesterday_shouldBeInvalid() {
        Instant beforeStartOfYesterday = LocalDate.now(ZoneOffset.UTC)
                .minusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .minusSeconds(1)
                .toInstant();

        boolean result = DateUtils.validateInstantForInterval(beforeStartOfYesterday, SleepIntervalType.START);

        assertFalse(result);
    }

    @Test
    void startInterval_exactlyAtStartOfYesterday_shouldBeValid() {
        Instant startOfYesterday = LocalDate.now(ZoneOffset.UTC)
                .minusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        boolean result = DateUtils.validateInstantForInterval(startOfYesterday, SleepIntervalType.START);

        assertTrue(result);
    }

    @Test
    void startInterval_yesterdayAfterStart_shouldBeValid() {
        Instant yesterdayLater = LocalDate.now(ZoneOffset.UTC)
                .minusDays(1)
                .atTime(12, 0)
                .toInstant(ZoneOffset.UTC);

        boolean result = DateUtils.validateInstantForInterval(yesterdayLater, SleepIntervalType.START);

        assertTrue(result);
    }

    @Test
    void startInterval_today_shouldBeValid() {
        Instant today = Instant.now();

        boolean result = DateUtils.validateInstantForInterval(today, SleepIntervalType.START);

        assertTrue(result);
    }
}
