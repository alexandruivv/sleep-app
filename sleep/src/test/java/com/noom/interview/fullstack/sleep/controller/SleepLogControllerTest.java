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
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
}
