package com.noom.interview.fullstack.sleep.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;

import static com.noom.interview.fullstack.sleep.utils.DateUtils.validateInstantForInterval;

public class SleepIntervalValidator implements ConstraintValidator<SleepIntervalConstraint, Instant> {

    private SleepIntervalType sleepInterval;

    @Override
    public void initialize(SleepIntervalConstraint constraintAnnotation) {
        this.sleepInterval = constraintAnnotation.sleepInterval();
    }

    @Override
    public boolean isValid(Instant instant, ConstraintValidatorContext ctx) {
        return validateInstantForInterval(instant, sleepInterval);
    }
}
