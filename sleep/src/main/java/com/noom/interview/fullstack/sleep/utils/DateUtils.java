package com.noom.interview.fullstack.sleep.utils;

import com.noom.interview.fullstack.sleep.validator.SleepIntervalType;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@UtilityClass
public class DateUtils {
    public static boolean validateInstantForInterval(Instant instant, SleepIntervalType sleepInterval) {
        if (instant == null) {
            return true;
        }

        ZoneId zone = ZoneOffset.UTC;

        if (sleepInterval == SleepIntervalType.END) {
            return !instant.isAfter(Instant.now());
        }

        if (sleepInterval == SleepIntervalType.START) {
            Instant startOfYesterday = LocalDate.now(zone)
                    .minusDays(1)
                    .atStartOfDay(zone)
                    .toInstant();

            return !instant.isBefore(startOfYesterday);
        }

        return true;
    }
}
