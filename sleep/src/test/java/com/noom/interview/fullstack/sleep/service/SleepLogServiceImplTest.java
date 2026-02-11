package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.entity.AppUserEntity;
import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.exception.SleepLogAlreadyExistsException;
import com.noom.interview.fullstack.sleep.mapper.SleepEntryMapper;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.repository.AppUserRepository;
import com.noom.interview.fullstack.sleep.repository.SleepEntityRepository;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SleepLogServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private SleepEntityRepository sleepEntityRepository;

    @Mock
    private SleepEntryMapper sleepEntryMapper;

    @Mock
    private SleepLogCalculatorService sleepLogCalculatorService;

    @InjectMocks
    private SleepLogServiceImpl service;

    @Test
    void createLastNightLog_createsRecord_whenNotExists_andUserExists() {
        UUID userId = UUID.randomUUID();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.GOOD);

        AppUserEntity user = AppUserEntity.builder().id(userId).build();

        SleepEntryEntity mappedEntity = SleepEntryEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .sleepDate(todayUtc)
                .build();

        SleepEntryEntity savedEntity = mappedEntity;

        SleepLogResponse response = SleepLogResponse.builder()
                .id(savedEntity.getId())
                .sleepDate(todayUtc)
                .timeInBedStart(request.getTimeInBedStart())
                .timeInBedEnd(request.getTimeInBedEnd())
                .totalTimeInBedMinutes(480)
                .morningFeeling(MorningFeeling.GOOD)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc)).thenReturn(Optional.empty());
        when(sleepEntryMapper.toNewEntity(request, user, todayUtc, 480)).thenReturn(mappedEntity);
        when(sleepEntityRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(sleepEntryMapper.toResponse(savedEntity)).thenReturn(response);

        SleepLogResponse actual = service.createLastNightLog(userId, request);

        assertThat(actual).isSameAs(response);

        verify(appUserRepository).findById(userId);
        verify(sleepEntityRepository).findByUserIdAndSleepDate(userId, todayUtc);
        verify(sleepEntryMapper).toNewEntity(request, user, todayUtc, 480);
        verify(sleepEntityRepository).save(mappedEntity);
        verify(sleepEntryMapper).toResponse(savedEntity);

        verifyNoMoreInteractions(appUserRepository, sleepEntityRepository, sleepEntryMapper);
    }

    @Test
    void createLastNightLog_createsUser_thenCreatesRecord_whenUserMissing() {
        UUID userId = UUID.randomUUID();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.OK);

        AppUserEntity savedUser = AppUserEntity.builder().id(userId).build();

        SleepEntryEntity mappedEntity = SleepEntryEntity.builder()
                .id(UUID.randomUUID())
                .user(savedUser)
                .sleepDate(todayUtc)
                .build();

        SleepLogResponse response = SleepLogResponse.builder()
                .id(mappedEntity.getId())
                .sleepDate(todayUtc)
                .totalTimeInBedMinutes(480)
                .morningFeeling(MorningFeeling.OK)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUserEntity.class))).thenReturn(savedUser);

        when(sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc)).thenReturn(Optional.empty());
        when(sleepEntryMapper.toNewEntity(request, savedUser, todayUtc, 480)).thenReturn(mappedEntity);
        when(sleepEntityRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(sleepEntryMapper.toResponse(mappedEntity)).thenReturn(response);

        SleepLogResponse actual = service.createLastNightLog(userId, request);

        assertThat(actual).isSameAs(response);

        verify(appUserRepository).findById(userId);

        ArgumentCaptor<AppUserEntity> userCaptor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(userId);

        verify(sleepEntityRepository).findByUserIdAndSleepDate(userId, todayUtc);
        verify(sleepEntryMapper).toNewEntity(request, savedUser, todayUtc, 480);
        verify(sleepEntityRepository).save(mappedEntity);
        verify(sleepEntryMapper).toResponse(mappedEntity);
    }

    @Test
    void createLastNightLog_throwsBadRequest_whenEndNotAfterStart() {
        UUID userId = UUID.randomUUID();

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-10T22:00:00Z"));
        request.setMorningFeeling(MorningFeeling.BAD);

        assertThatThrownBy(() -> service.createLastNightLog(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timeInBedEnd must be after timeInBedStart");

        verifyNoInteractions(appUserRepository, sleepEntityRepository, sleepEntryMapper);
    }

    @Test
    void createLastNightLog_throwsConflict_whenRecordAlreadyExists() {
        UUID userId = UUID.randomUUID();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        CreateSleepLogRequest request = new CreateSleepLogRequest();
        request.setTimeInBedStart(Instant.parse("2026-02-10T22:00:00Z"));
        request.setTimeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"));
        request.setMorningFeeling(MorningFeeling.GOOD);

        AppUserEntity user = AppUserEntity.builder().id(userId).build();
        SleepEntryEntity existing = SleepEntryEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .sleepDate(todayUtc)
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createLastNightLog(userId, request))
                .isInstanceOf(SleepLogAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(appUserRepository).findById(userId);
        verify(sleepEntityRepository).findByUserIdAndSleepDate(userId, todayUtc);

        verifyNoMoreInteractions(appUserRepository, sleepEntityRepository);
        verifyNoInteractions(sleepEntryMapper);
    }

    @Test
    void getLastNightLog_returnsResponse_whenFound() {
        UUID userId = UUID.randomUUID();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        SleepEntryEntity entity = SleepEntryEntity.builder()
                .id(UUID.randomUUID())
                .sleepDate(todayUtc)
                .timeInBedStart(Instant.parse("2026-02-10T22:00:00Z"))
                .timeInBedEnd(Instant.parse("2026-02-11T06:00:00Z"))
                .totalTimeInBedMinutes(480)
                .morningFeeling(MorningFeeling.GOOD)
                .build();

        SleepLogResponse response = SleepLogResponse.builder()
                .id(entity.getId())
                .sleepDate(todayUtc)
                .totalTimeInBedMinutes(480)
                .morningFeeling(MorningFeeling.GOOD)
                .build();

        when(sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc))
                .thenReturn(Optional.of(entity));
        when(sleepEntryMapper.toResponse(entity)).thenReturn(response);

        SleepLogResponse actual = service.getLastNightLog(userId);

        assertThat(actual).isSameAs(response);

        verify(sleepEntityRepository).findByUserIdAndSleepDate(userId, todayUtc);
        verify(sleepEntryMapper).toResponse(entity);
        verifyNoMoreInteractions(sleepEntityRepository, sleepEntryMapper);
        verifyNoInteractions(appUserRepository);
    }

    @Test
    void getLastNightLog_throwsNotFound_whenMissing() {
        UUID userId = UUID.randomUUID();
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);

        when(sleepEntityRepository.findByUserIdAndSleepDate(userId, todayUtc))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLastNightLog(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(sleepEntityRepository).findByUserIdAndSleepDate(userId, todayUtc);
        verifyNoMoreInteractions(sleepEntryMapper);
        verifyNoInteractions(sleepEntryMapper, appUserRepository);
    }

    @Test
    void shouldReturnLast30DayAverages() {
        UUID userId = UUID.randomUUID();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        List<SleepEntryEntity> sleepEntries = List.of(
                new SleepEntryEntity(),
                new SleepEntryEntity()
        );

        Map<MorningFeeling, SleepLogAveragesResponse.FeelingFrequency> mockedFrequencies = Map.of(
                MorningFeeling.GOOD, SleepLogAveragesResponse.FeelingFrequency.builder()
                        .count(5)
                        .percentage(new BigDecimal("71.43"))
                        .build(),
                MorningFeeling.BAD, SleepLogAveragesResponse.FeelingFrequency.builder()
                        .count(2)
                        .percentage(new BigDecimal("28.57"))
                        .build()
        );

        when(sleepEntityRepository.findByUserIdAndSleepDateBetween(
                eq(userId), eq(thirtyDaysAgo), eq(today)))
                .thenReturn(sleepEntries);

        when(sleepLogCalculatorService.calculateAverageTimeInBed(sleepEntries))
                .thenReturn(420);

        when(sleepLogCalculatorService.calculateAverageTimeUserGetsInBed(sleepEntries))
                .thenReturn(LocalTime.of(23, 15));

        when(sleepLogCalculatorService.calculateAverageTimeUserGetsOutOfBed(sleepEntries))
                .thenReturn(LocalTime.of(7, 30));

        when(sleepLogCalculatorService.calculateMorningFrequencies(sleepEntries))
                .thenReturn(mockedFrequencies);

        SleepLogAveragesResponse response = service.getLast30DayAverages(userId);

        assertNotNull(response);
        assertEquals(thirtyDaysAgo, response.getRangeStart());
        assertEquals(today, response.getRangeEnd());
        assertEquals(420, response.getAverageTimeInBedMinutes());
        assertEquals(LocalTime.of(23, 15), response.getAverageTimeUserGetsInBed());
        assertEquals(LocalTime.of(7, 30), response.getAverageTimeUserGetsOutOfBed());
        assertEquals(mockedFrequencies, response.getMorningFeelingFrequencies());

        verify(sleepEntityRepository)
                .findByUserIdAndSleepDateBetween(eq(userId), eq(thirtyDaysAgo), eq(today));

        verify(sleepLogCalculatorService).calculateAverageTimeInBed(sleepEntries);
        verify(sleepLogCalculatorService).calculateAverageTimeUserGetsInBed(sleepEntries);
        verify(sleepLogCalculatorService).calculateAverageTimeUserGetsOutOfBed(sleepEntries);
        verify(sleepLogCalculatorService).calculateMorningFrequencies(sleepEntries);

        verifyNoMoreInteractions(sleepEntityRepository, sleepLogCalculatorService);
    }

    @Test
    void shouldThrowWhenNoSleepLogsFound() {
        UUID userId = UUID.randomUUID();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        when(sleepEntityRepository.findByUserIdAndSleepDateBetween(
                eq(userId), eq(thirtyDaysAgo), eq(today)))
                .thenReturn(List.of());

        assertThrows(EntityNotFoundException.class,
                () -> service.getLast30DayAverages(userId));

        verify(sleepEntityRepository)
                .findByUserIdAndSleepDateBetween(eq(userId), eq(thirtyDaysAgo), eq(today));

        verifyNoInteractions(sleepLogCalculatorService);
    }
}
