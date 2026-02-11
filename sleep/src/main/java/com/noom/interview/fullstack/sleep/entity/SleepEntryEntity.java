package com.noom.interview.fullstack.sleep.entity;

import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sleep_entry")
public class SleepEntryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;

    @Column(name = "time_in_bed_start", nullable = false)
    private Instant timeInBedStart;

    @Column(name = "time_in_bed_end", nullable = false)
    private Instant timeInBedEnd;

    @Column(name = "total_time_in_bed_minutes", nullable = false)
    private int totalTimeInBedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "morning_feeling", nullable = false, length = 4)
    private MorningFeeling morningFeeling;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
