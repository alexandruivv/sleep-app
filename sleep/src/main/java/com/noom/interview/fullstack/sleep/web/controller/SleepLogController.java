package com.noom.interview.fullstack.sleep.web.controller;

import com.noom.interview.fullstack.sleep.service.SleepLogService;
import com.noom.interview.fullstack.sleep.service.UserService;
import com.noom.interview.fullstack.sleep.web.requests.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.web.responses.SleepLogAveragesResponse;
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

    @GetMapping
    @Operation(
            summary = "Get last night's sleep log",
            description = "Returns today's sleep log for the current user.",
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
    @ApiResponse(responseCode = "200", description = "Sleep log found",
            content = @Content(schema = @Schema(implementation = SleepLogResponse.class)))
    @ApiResponse(responseCode = "404", description = "Sleep log not found")
    @ApiResponse(responseCode = "400", description = "Missing or invalid X-User-Id header")
    public SleepLogResponse getLastNightSleepLog() {
        UUID userId = userService.getCurrentUserId();
        return sleepLogService.getLastNightLog(userId);
    }

    @GetMapping("/averages/last-30-days")
    @Operation(
            summary = "Get last 30-day averages",
            description = "Returns averages for the last 30 days (inclusive) in UTC.",
            parameters = {
                    @Parameter(
                            name = "X-User-Id",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "User identifier (UUID)",
                            schema = @Schema(type = "string", format = "uuid", example = "11111111-1111-1111-1111-111111111111")
                    )
            }
    )
    @ApiResponse(responseCode = "200", description = "Averages returned",
            content = @Content(schema = @Schema(implementation = SleepLogAveragesResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing/invalid user header", content = @Content)
    public SleepLogAveragesResponse getLast30DayAverages() {
        UUID userId = userService.getCurrentUserId();
        return sleepLogService.getLast30DayAverages(userId);
    }
}
