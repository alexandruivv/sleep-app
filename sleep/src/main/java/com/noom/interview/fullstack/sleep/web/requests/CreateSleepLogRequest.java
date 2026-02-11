package com.noom.interview.fullstack.sleep.web.requests;

import com.noom.interview.fullstack.sleep.model.MorningFeeling;
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
    @Schema(description = "Time when the user went to bed (UTC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timeInBedStart;
    @NotNull
    @Schema(description = "Time when the user got out of bed (UTC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timeInBedEnd;
    @NotNull
    @Schema(
            description = "How the user felt in the morning",
            requiredMode = Schema.RequiredMode.REQUIRED
    )

    private MorningFeeling morningFeeling;
}
