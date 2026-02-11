package com.noom.interview.fullstack.sleep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.repository.SleepEntityRepository;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Tag("it")
class SleepLogControllerIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13-alpine")
            .withDatabaseName("sleepdb")
            .withUsername("sleep")
            .withPassword("sleep");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SleepEntityRepository sleepEntityRepository;

    @Test
    void createsTodaysSleepLog_andSecondCreateReturnsConflict() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.GOOD);

        mockMvc.perform(post("/sleep-log")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(480));

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        var recordOpt = sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc);
        assertThat(recordOpt).isPresent();
        assertThat(recordOpt.get().getTotalTimeInBedMinutes()).isEqualTo(480);
        assertThat(recordOpt.get().getMorningFeeling().name()).isEqualTo("GOOD");

        mockMvc.perform(post("/sleep-log")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getLastNight_returns404_whenNoRecord() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/sleep-log/last-night")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createThenGetLastNight_returns200_withSameData() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateSleepLogRequest create = new CreateSleepLogRequest();
        create.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        create.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        create.setMorningFeeling(MorningFeeling.GOOD);

        mockMvc.perform(post("/sleep-log")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(480))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"));

        mockMvc.perform(get("/sleep-log")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTimeInBedMinutes").value(480))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"));
    }

    @Test
    void getLastNight_returns400_whenHeaderMissing() throws Exception {
        mockMvc.perform(get("/sleep-log"))
                .andExpect(status().isBadRequest());
    }
}
