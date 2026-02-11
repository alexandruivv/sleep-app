package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.entity.AppUserEntity;
import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.exception.SleepLogAlreadyExistsException;
import com.noom.interview.fullstack.sleep.mapper.SleepEntryMapper;
import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.repository.AppUserRepository;
import com.noom.interview.fullstack.sleep.repository.SleepEntityRepository;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
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
}
