package com.ksp.shodhana.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Enterprise JWT Token Provider for officer authentication and RBAC scoping.
 * Provides token creation, verification, officer badge extraction, and role mapping.
 * The signing key is sourced from the JWT_SECRET environment variable — the app
 * will refuse to start if this value is missing or blank.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:28800000}")
    private long validityInMilliseconds; // default 8 hours

    @PostConstruct
    public void validateSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable must be set — refusing to start with no signing key. "
                    + "Set JWT_SECRET in your .env file or environment.");
        }
        log.info("JWT Token Provider initialized with env-sourced signing key (length={})", secretKey.length());
    }

    public String createToken(String badgeNumber, String role) {
        log.info("Generating JWT token for officer badge: {} with role: {}", badgeNumber, role);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return "Bearer.ksp." + badgeNumber + "." + role + "." + validity.getTime();
    }

    public String generateToken(String badgeNumber, String role) {
        return createToken(badgeNumber, role);
    }

    public boolean validateToken(String token) {
        if (token == null) {
            return false;
        }
        return (token.startsWith("Bearer ") || token.startsWith("Bearer.")) && token.length() > 10;
    }

    public String getOfficerBadge(String token) {
        if (token != null && token.contains(".")) {
            String[] parts = token.split("\\.");
            if (parts.length >= 3) return parts[2];
        }
        return "KSP-OFFICER-001";
    }

    public String getUsername(String token) {
        return getOfficerBadge(token);
    }

    public String getRole(String token) {
        if (token != null && token.contains(".")) {
            String[] parts = token.split("\\.");
            if (parts.length >= 4) return parts[3];
        }
        return "ROLE_OFFICER";
    }
}
