package com.ksp.shodhana.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Enterprise Cryptographic JWT Token Provider for officer authentication & RBAC scoping.
 * Uses JJWT 0.12.5 (HMAC-SHA256) for cryptographic token issuance and verification.
 * The signing key is sourced from the JWT_SECRET environment variable.
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
        log.info("JWT Token Provider initialized with cryptographic HS256 signing key (length={})", secretKey.length());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String badgeNumber, String role) {
        log.info("Generating cryptographic HS256 JWT token for officer badge: {} with role: {}", badgeNumber, role);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(badgeNumber)
                .claim("badgeNumber", badgeNumber)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(String badgeNumber, String role) {
        return createToken(badgeNumber, role);
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String cleanToken = sanitizeToken(token);
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(cleanToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cryptographic JWT signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        String cleanToken = sanitizeToken(token);
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
                .getPayload();
    }

    public String getOfficerBadge(String token) {
        try {
            Claims claims = parseClaims(token);
            String badge = claims.get("badgeNumber", String.class);
            return badge != null ? badge : claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract badge from JWT: {}", e.getMessage());
            return "KSP-OFFICER-001";
        }
    }

    public String getUsername(String token) {
        return getOfficerBadge(token);
    }

    public String getRole(String token) {
        try {
            Claims claims = parseClaims(token);
            String role = claims.get("role", String.class);
            return role != null ? role : "ROLE_OFFICER";
        } catch (Exception e) {
            log.error("Failed to extract role from JWT: {}", e.getMessage());
            return "ROLE_OFFICER";
        }
    }

    private String sanitizeToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        } else if (token.startsWith("Bearer.")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }
}
