package com.noom.interview.fullstack.sleep.web.requests;

import com.noom.interview.fullstack.sleep.model.MorningFeeling;
import com.noom.interview.fullstack.sleep.validator.SleepIntervalConstraint;
import com.noom.interview.fullstack.sleep.validator.SleepIntervalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSleepLogRequest {
    @NotNull
    @SleepIntervalConstraint(message = "Invalid value, should not be older than one day before", sleepInterval = SleepIntervalType.START)
    @Schema(description = "Time when the user went to bed (UTC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timeInBedStart;
    @NotNull
    @SleepIntervalConstraint(message = "Invalid value, should not be after current date", sleepInterval = SleepIntervalType.END)
    @Schema(description = "Time when the user got out of bed (UTC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timeInBedEnd;
    @NotNull
    @Schema(
            description = "How the user felt in the morning",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private MorningFeeling morningFeeling;
}
