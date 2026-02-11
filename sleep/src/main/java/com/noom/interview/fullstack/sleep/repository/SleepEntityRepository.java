package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.entity.SleepEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SleepEntityRepository extends JpaRepository<SleepEntryEntity, UUID> {
    Optional<SleepEntryEntity> findByUserIdAndSleepDate(UUID userId, LocalDate sleepDate);
}
