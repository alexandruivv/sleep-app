package com.noom.interview.fullstack.sleep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.exception.MissingUserIdHeaderException;
import com.noom.interview.fullstack.sleep.exception.SleepLogAlreadyExistsException;
import com.noom.interview.fullstack.sleep.filter.UserContext;
import com.noom.interview.fullstack.sleep.filter.UserContextFilter;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserService;
import com.noom.interview.fullstack.sleep.web.controller.SleepLogController;
import com.noom.interview.fullstack.sleep.web.exception.ApiExceptionHandler;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SleepLogController.class)
@Import({UserContextFilter.class, UserContext.class, ApiExceptionHandler.class})
class SleepLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SleepLogService sleepLogService;

    @Test
    void createSleepLog_returns201_whenValid() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.GOOD);

        SleepLogResponse response = SleepLogResponse.builder()
                .id(UUID.randomUUID())
                .sleepDate(LocalDate.parse("2026-02-11"))
                .timeInBedStart(request.getTimeInBedStart())
                .timeInBedEnd(request.getTimeInBedEnd())
                .totalTimeInBedMinutes(480)
                .morningFeeling(MorningFeeling.GOOD)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(sleepLogService.createLastNightLog(eq(userId), any(CreateSleepLogRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/sleep-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(480))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"));
    }

    @Test
    void createSleepLog_returns400_whenBodyInvalid() throws Exception {
        CreateSleepLogRequest request = new CreateSleepLogRequest();

        mockMvc.perform(post("/sleep-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSleepLog_returns400_whenMissingUserHeader() throws Exception {
        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.OK);

        when(userService.getCurrentUserId())
                .thenThrow(new MissingUserIdHeaderException("Missing X-User-Id header"));

        mockMvc.perform(post("/sleep-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSleepLog_returns409_whenLogAlreadyExists() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.BAD);

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(sleepLogService.createLastNightLog(eq(userId), any(CreateSleepLogRequest.class)))
                .thenThrow(new SleepLogAlreadyExistsException());

        mockMvc.perform(post("/sleep-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getLastNight_returns200_whenExists() throws Exception {
        UUID userId = UUID.randomUUID();

        SleepLogResponse response = SleepLogResponse.builder()
                .id(UUID.randomUUID())
                .sleepDate(LocalDate.now())
                .timeInBedStart(Instant.parse("2026-02-10T22:00:00Z"))
                .timeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"))
                .totalTimeInBedMinutes(480)
                .morningFeeling(com.noom.interview.fullstack.sleep.model.MorningFeeling.GOOD)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(sleepLogService.getLastNightLog(userId)).thenReturn(response);

        mockMvc.perform(get("/sleep-log")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(480))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"));
    }

    @Test
    void getLastNight_returns404_whenMissing() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(sleepLogService.getLastNightLog(userId))
                .thenThrow(new EntityNotFoundException("Sleep log for today not found"));

        mockMvc.perform(get("/sleep-log"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastNight_returns400_whenUserHeaderMissing() throws Exception {
        when(userService.getCurrentUserId())
                .thenThrow(new MissingUserIdHeaderException("Missing X-User-Id header"));

        mockMvc.perform(get("/sleep-log"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLast30DayAverages_returns200() throws Exception {
        UUID userId = UUID.randomUUID();

        Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> freq = new EnumMap<>(MorningFeeling.class);
        freq.put(MorningFeeling.GOOD, SleepLogAveragesResponse.FeelingFrequency.builder()
                .count(3).percentage(BigDecimal.valueOf(50.00)).build());
        freq.put(MorningFeeling.OK, SleepLogAveragesResponse.FeelingFrequency.builder()
                .count(1).percentage(BigDecimal.valueOf(16.67)).build());
        freq.put(MorningFeeling.BAD, SleepLogAveragesResponse.FeelingFrequency.builder()
                .count(2).percentage(BigDecimal.valueOf(33.33)).build());

        SleepLogAveragesResponse response = SleepLogAveragesResponse.builder()
                .rangeStart(LocalDate.of(2026, 1, 13))
                .rangeEnd(LocalDate.of(2026, 2, 11))
                .averageTimeInBedMinutes(450)
                .averageTimeUserGetsInBed(LocalTime.of(22, 40))
                .averageTimeUserGetsOutOfBed(LocalTime.of(6, 40))
                .morningFeelingFrequencies(freq)
                .build();

        when(userService.getCurrentUserId()).thenReturn(userId);
        when(sleepLogService.getLast30DayAverages(eq(userId))).thenReturn(response);

        mockMvc.perform(get("/sleep-log/averages/last-30-days")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rangeStart").value("2026-01-13"))
                .andExpect(jsonPath("$.rangeEnd").value("2026-02-11"))
                .andExpect(jsonPath("$.averageTimeUserGetsInBed").value("22:40:00"))
                .andExpect(jsonPath("$.averageTimeUserGetsOutOfBed").value("06:40:00"))
                .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD.count").value(3))
                .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD.percentage").value(50.00));
    }

    @Test
    void getLast30DayAverages_returns400_whenUserMissing() throws Exception {
        when(userService.getCurrentUserId())
                .thenThrow(new MissingUserIdHeaderException("Missing X-User-Id header"));

        mockMvc.perform(get("/sleep-log/averages/last-30-days"))
                .andExpect(status().isBadRequest());
    }
}
