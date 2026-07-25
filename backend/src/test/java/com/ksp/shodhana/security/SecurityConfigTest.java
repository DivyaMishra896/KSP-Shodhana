package com.ksp.shodhana.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Verify Cryptographic JWT Token Generation and Signature Verification")
    public void testJwtTokenGenerationAndValidation() {
        String token = jwtTokenProvider.generateToken("KSP-OFFICER-7892", "ROLE_INSPECTOR");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("KSP-OFFICER-7892", jwtTokenProvider.getUsername(token));
        assertEquals("ROLE_INSPECTOR", jwtTokenProvider.getRole(token));
    }

    @Test
    @DisplayName("Verify Invalid Token Rejection")
    public void testInvalidTokenRejection() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token.string"));
    }

    @Test
    @DisplayName("Verify Forged Token Rejection (Signature Mismatch Failure)")
    public void testForgedTokenRejection() {
        // 1. Issue a valid token for ROLE_OFFICER
        String validToken = jwtTokenProvider.generateToken("KSP-OFFICER-7892", "ROLE_OFFICER");
        assertTrue(jwtTokenProvider.validateToken(validToken), "Valid token must pass verification");

        // 2. Tamper with token payload (attempt role escalation to ROLE_SUPERINTENDENT without re-signing)
        String[] parts = validToken.split("\\.");
        assertEquals(3, parts.length, "JWT must contain 3 parts: header.payload.signature");

        String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]));
        String forgedPayload = decodedPayload.replace("ROLE_OFFICER", "ROLE_SUPERINTENDENT");
        String encodedForgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(forgedPayload.getBytes());

        String forgedToken = parts[0] + "." + encodedForgedPayload + "." + parts[2];

        // 3. Assert cryptographic validation REJECTS the forged token
        assertFalse(jwtTokenProvider.validateToken(forgedToken), "Forged token with altered payload MUST be rejected due to signature mismatch");
    }
}
