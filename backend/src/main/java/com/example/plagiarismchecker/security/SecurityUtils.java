package com.example.plagiarismchecker.security;

import com.example.plagiarismchecker.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper for reading the currently authenticated user out of the
 * Spring Security context inside services/controllers.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }
        return principal;
    }
}
