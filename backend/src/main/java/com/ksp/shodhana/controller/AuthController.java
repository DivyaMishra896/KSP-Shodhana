package com.ksp.shodhana.controller;

import com.ksp.shodhana.dto.response.ApiResponse;
import com.ksp.shodhana.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing real Cryptographic JJWT Token Generation & Verification
 * for RBAC Role Switcher & Authorization headers.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/token")
    public ApiResponse<TokenResponsePayload> generateToken(@RequestBody(required = false) TokenRequestPayload request) {
        String officerBadge = (request != null && request.getOfficerBadge() != null) ? request.getOfficerBadge() : "KSP-SP-9912";
        String role = (request != null && request.getRole() != null) ? request.getRole() : "ROLE_SUPERINTENDENT";

        String token = jwtTokenProvider.createToken(officerBadge, role);
        boolean isValid = jwtTokenProvider.validateToken(token);

        return ApiResponse.ok(TokenResponsePayload.builder()
                .token(token)
                .officerBadge(officerBadge)
                .role(role)
                .isValid(isValid)
                .signatureType("HS256 HMAC Cryptographic Signature")
                .build());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRequestPayload {
        private String officerBadge;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenResponsePayload {
        private String token;
        private String officerBadge;
        private String role;
        private boolean isValid;
        private String signatureType;
    }
}
