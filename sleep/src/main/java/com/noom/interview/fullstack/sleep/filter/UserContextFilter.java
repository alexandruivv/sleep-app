package com.noom.interview.fullstack.sleep.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final String USER_HEADER = "X-User-Id";

    private final UserContext userContext;

    public UserContextFilter(UserContext userContext) {
        this.userContext = userContext;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String headerValue = request.getHeader(USER_HEADER);

        if (headerValue != null && !headerValue.isBlank()) {
            try {
                userContext.setUserId(UUID.fromString(headerValue));
            } catch (IllegalArgumentException ex) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid X-User-Id header"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
