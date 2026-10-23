package com.shreyas.worklynk_ai.security;

/** The logged-in user, rebuilt from the JWT on every request. */
public record AuthUser(
        Long employeeId,
        String email,
        String role,
        Long companyId) {
}
