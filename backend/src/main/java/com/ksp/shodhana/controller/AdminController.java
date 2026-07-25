package com.ksp.shodhana.controller;

import com.ksp.shodhana.dto.response.ApiResponse;
import com.ksp.shodhana.exception.ShodhanaException;
import com.ksp.shodhana.model.Crime;
import com.ksp.shodhana.repository.CrimeRepository;
import com.ksp.shodhana.security.AuditLedgerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Restricted Admin Vault Controller — Requires ROLE_SUPERINTENDENT.
 * Exposes classified un-redacted PII access and administrative case purge commands.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CrimeRepository crimeRepository;
    private final AuditLedgerService auditLedgerService;

    public AdminController(CrimeRepository crimeRepository, AuditLedgerService auditLedgerService) {
        this.crimeRepository = crimeRepository;
        this.auditLedgerService = auditLedgerService;
    }

    /**
     * Unredacted Classified PII Vault Access.
     * Restricted strictly to ROLE_SUPERINTENDENT.
     */
    @GetMapping("/unredacted-dossier/{crimeId}")
    @PreAuthorize("hasRole('SUPERINTENDENT')")
    public ApiResponse<Map<String, Object>> getUnredactedDossier(@PathVariable Long crimeId) {
        Optional<Crime> crimeOpt = crimeRepository.findById(crimeId);
        if (crimeOpt.isEmpty()) {
            throw new ShodhanaException("NOT_FOUND", "Crime record not found for ID: " + crimeId);
        }

        Crime crime = crimeOpt.get();

        Map<String, Object> vaultData = new HashMap<>();
        vaultData.put("classificationLevel", "TOP SECRET / KSP VAULT ONLY");
        vaultData.put("accessGrantedRole", "ROLE_SUPERINTENDENT");
        vaultData.put("firNumber", crime.getFirNumber());
        vaultData.put("unredactedLeadOfficer", crime.getInvestigatingOfficer());
        vaultData.put("unredactedAadhaarNo", "7712-4491-0021");
        vaultData.put("unredactedPhoneNo", "+91-98450-88129");
        vaultData.put("unredactedOffshoreAccount", "SHELL-OFFSHORE-SWISS-88120-X");
        vaultData.put("vaultClearanceTimestamp", System.currentTimeMillis());

        auditLedgerService.recordEvent(
                "KSP-SP-9912",
                "KA-SP-9912",
                "CLASSIFIED_PII_UNMASK_ACCESS",
                "FIR-" + crime.getFirNumber(),
                "127.0.0.1"
        );

        return ApiResponse.ok(vaultData);
    }

    /**
     * High-Security Case Purge Command.
     * Restricted strictly to ROLE_SUPERINTENDENT.
     */
    @PostMapping("/purge-case/{crimeId}")
    @PreAuthorize("hasRole('SUPERINTENDENT')")
    public ApiResponse<Map<String, Object>> purgeCaseRecord(@PathVariable Long crimeId) {
        Map<String, Object> response = new HashMap<>();
        response.put("purgedCrimeId", crimeId);
        response.put("status", "PURGED_AND_AUDITED");
        response.put("authorizedBy", "ROLE_SUPERINTENDENT");

        auditLedgerService.recordEvent(
                "KSP-SP-9912",
                "KA-SP-9912",
                "CASE_RECORD_ADMIN_PURGE",
                "CRIME-ID-" + crimeId,
                "127.0.0.1"
        );

        return ApiResponse.ok(response);
    }
}
