package com.noom.interview.fullstack.sleep.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Getter
@Setter
@Component
@RequestScope
public class UserContext {
    private UUID userId;
}
