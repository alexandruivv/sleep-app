package com.noom.interview.fullstack.sleep.mapper;

import com.noom.interview.fullstack.sleep.entity.AppUserEntity;
import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface SleepEntryMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "sleepDate", source = "sleepDate")
    @Mapping(target = "totalTimeInBedMinutes", source = "totalMinutes")
    @Mapping(target = "morningFeeling", source = "request.morningFeeling")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SleepEntryEntity toNewEntity(
            CreateSleepLogRequest request,
            AppUserEntity user,
            LocalDate sleepDate,
            int totalMinutes
    );

    @Mapping(target = "morningFeeling", source = "morningFeeling")
    SleepLogResponse toResponse(SleepEntryEntity entity);
}
