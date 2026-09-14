package com.feel.backend.auth;

public enum AuthRole {
    ADMIN,
    USER;

    public static AuthRole fromClaim(String claim) {
        if (claim == null || claim.isBlank()) {
            return null;
        }
        try {
            return AuthRole.valueOf(claim.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
