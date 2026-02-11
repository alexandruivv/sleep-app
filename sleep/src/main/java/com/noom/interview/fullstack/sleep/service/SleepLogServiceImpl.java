package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.entity.AppUserEntity;
import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.exception.SleepLogAlreadyExistsException;
import com.noom.interview.fullstack.sleep.exception.ValidationException;
import com.noom.interview.fullstack.sleep.mapper.SleepEntryMapper;
import com.noom.interview.fullstack.sleep.repository.AppUserRepository;
import com.noom.interview.fullstack.sleep.repository.SleepEntityRepository;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SleepLogServiceImpl implements SleepLogService {
    private final AppUserRepository appUserRepository;
    private final SleepEntityRepository sleepEntityRepository;
    private final SleepEntryMapper sleepEntryMapper;

    @Override
    @Transactional
    public SleepLogResponse createLastNightLog(UUID userId, CreateSleepLogRequest request) {
        if (!request.getTimeInBedEnd().isAfter(request.getTimeInBedStart())) {
            throw new ValidationException("timeInBedEnd must be after timeInBedStart");
        }

        long minutesLong = Duration.between(request.getTimeInBedStart(), request.getTimeInBedEnd()).toMinutes();

        int totalMinutes = Math.toIntExact(minutesLong);

        LocalDate sleepDate = LocalDate.now(ZoneOffset.UTC);

        AppUserEntity user = appUserRepository.findById(userId)
                .orElseGet(() -> appUserRepository.save(AppUserEntity.builder().id(userId).build()));

        boolean alreadyExists = sleepEntityRepository
                .findByUserIdAndSleepDate(user.getId(), sleepDate)
                .isPresent();
        if (alreadyExists) {
            throw new SleepLogAlreadyExistsException();
        }

        var entity = sleepEntryMapper.toNewEntity(
                request,
                user,
                sleepDate,
                totalMinutes
        );

        var saved = sleepEntityRepository.save(entity);

        return sleepEntryMapper.toResponse(saved);
    }

    @Override
    public SleepLogResponse getLastNightLog(UUID userId) {
        LocalDate sleepDate = LocalDate.now(ZoneOffset.UTC);

        SleepEntryEntity entity = sleepEntityRepository
                .findByUserIdAndSleepDate(userId, sleepDate)
                .orElseThrow(() ->
                        new EntityNotFoundException("Sleep log for today not found")
                );

        return sleepEntryMapper.toResponse(entity);
    }
}
