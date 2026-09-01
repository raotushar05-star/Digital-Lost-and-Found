package com.lostandfound.security;

import com.lostandfound.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ForbiddenException("No authenticated user found");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        return getCurrentPrincipal().getUserId();
    }

    public static String getCurrentRole() {
        return getCurrentPrincipal().getRole();
    }

    public static boolean isPolice() {
        String role = getCurrentRole();
        return "POLICE_OFFICER".equals(role) || "POLICE_ADMIN".equals(role);
    }

    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "POLICE_ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role);
    }
}
