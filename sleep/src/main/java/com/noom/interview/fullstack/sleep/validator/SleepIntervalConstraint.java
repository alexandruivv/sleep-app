package com.noom.interview.fullstack.sleep.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SleepIntervalValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SleepIntervalConstraint {
    String message() default "Invalid value for interval";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    SleepIntervalType sleepInterval() default SleepIntervalType.START;
}