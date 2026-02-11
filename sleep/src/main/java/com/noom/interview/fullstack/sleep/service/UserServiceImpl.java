package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.exception.MissingUserIdHeaderException;
import com.noom.interview.fullstack.sleep.filter.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserContext userContext;

    @Override
    public UUID getCurrentUserId() {
        UUID userId = userContext.getUserId();
        if (userId == null) {
            throw new MissingUserIdHeaderException("Missing X-User-Id header");
        }
        return userId;
    }
}
