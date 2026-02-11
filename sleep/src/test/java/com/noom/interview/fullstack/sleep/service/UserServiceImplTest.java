package com.noom.interview.fullstack.sleep.service;

import com.noom.interview.fullstack.sleep.exception.MissingUserIdHeaderException;
import com.noom.interview.fullstack.sleep.filter.UserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserContext userContext;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUserId_returnsUserId_whenPresentInContext() {
        UUID expectedUserId = UUID.randomUUID();
        when(userContext.getUserId()).thenReturn(expectedUserId);

        UUID actualUserId = userService.getCurrentUserId();

        assertThat(actualUserId).isEqualTo(expectedUserId);
        verify(userContext).getUserId();
        verifyNoMoreInteractions(userContext);
    }

    @Test
    void getCurrentUserId_throwsException_whenUserIdMissing() {
        when(userContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> userService.getCurrentUserId())
                .isInstanceOf(MissingUserIdHeaderException.class)
                .hasMessageContaining("Missing X-User-Id header");

        verify(userContext).getUserId();
        verifyNoMoreInteractions(userContext);
    }
}
