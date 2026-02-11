package com.noom.interview.fullstack.sleep.web.controller;

import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserService;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/sleep-log")
@RequiredArgsConstructor
public class SleepLogController {
    private final UserService userService;
    private final SleepLogService sleepLogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create sleep log for last night",
            description = "Creates today's sleep log for the current user. ",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "User identifier (UUID)",
                            schema = @Schema(type = "string", format = "uuid")
                    )
            }
    )
    @ApiResponse(responseCode = "201", description = "Sleep log created",
            content = @Content(schema = @Schema(implementation = SleepLogResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request (missing header, null fields, or invalid interval)",
            content = @Content)
    public SleepLogResponse createSleepLog(
            @Valid @RequestBody CreateSleepLogRequest request
    ) {
        UUID userId = userService.getCurrentUserId();
        return sleepLogService.createLastNightLog(userId, request);
    }
}
