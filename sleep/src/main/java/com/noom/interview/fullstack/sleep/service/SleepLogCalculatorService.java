package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface SleepLogCalculatorService {
    /**
     * Calculates the average time spent in bed for a list of sleep entries
     * @param sleepEntryEntities sleep entries retrieved from database
     * @return average time in minutes
     */
    int calculateAverageTimeInBed(List<SleepEntryEntity> sleepEntryEntities);

    /**
     * Calculates the average time the user gets in bed by converting the hours in epoch seconds, calculating
     * the average and converting back to hours in UTC
     * @param sleepEntryEntities sleep entries retrieved from database
     * @return local time UTC which can be read in format HH:mm
     */
    LocalTime calculateAverageTimeUserGetsInBed(List<SleepEntryEntity> sleepEntryEntities);

    /**
     * Calculates the average time the user gets out of bed by converting the hours in epoch seconds, calculating
     * the average and converting back to hours in UTC
     * @param sleepEntryEntities sleep entries retrieved from database
     * @return local time UTC which can be read in format HH:mm
     */
    LocalTime calculateAverageTimeUserGetsOutOfBed(List<SleepEntryEntity> sleepEntryEntities);

    /**
     * Calculates frequencies of how the user felt in the morning by mentioning the feeling and the count + percentage
     * of each value
     * @param sleepEntryEntities sleep entries retrieved from database
     * @return a map containing for each feeling value the count and percentage found
     */
    Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> calculateMorningFrequencies(List<SleepEntryEntity> sleepEntryEntities);
}
