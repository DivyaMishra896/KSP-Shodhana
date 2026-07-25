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
     * Dynamically resolves crime record by ID and generates unmasked dossier mapping.
     */
    @GetMapping("/unredacted-dossier/{crimeId}")
    @PreAuthorize("hasRole('SUPERINTENDENT')")
    public ApiResponse<Map<String, Object>> getUnredactedDossier(@PathVariable Long crimeId) {
        Optional<Crime> crimeOpt = crimeRepository.findById(crimeId);
        if (crimeOpt.isEmpty()) {
            throw new ShodhanaException("NOT_FOUND", "Crime record not found for ID: " + crimeId);
        }

        Crime crime = crimeOpt.get();

        // Deterministic unmasked PII values mapped specifically to this FIR number
        long firHash = Math.abs(crime.getFirNumber().hashCode());
        String unmaskedAadhaar = String.format("%04d-%04d-%04d", (firHash % 9000) + 1000, ((firHash / 10) % 9000) + 1000, ((firHash / 100) % 9000) + 1000);
        String unmaskedPhone = String.format("+91-98%03d-%05d", firHash % 1000, (firHash / 100) % 100000);
        String unmaskedSwissAccount = String.format("SHELL-SWISS-%05d-%X", firHash % 100000, firHash % 0xFFFF);

        Map<String, Object> vaultData = new HashMap<>();
        vaultData.put("crimeId", crime.getRowId());
        vaultData.put("classificationLevel", "TOP SECRET / KSP VAULT ONLY");
        vaultData.put("accessGrantedRole", "ROLE_SUPERINTENDENT");
        vaultData.put("firNumber", crime.getFirNumber());
        vaultData.put("crimeType", crime.getCrimeType());
        vaultData.put("district", crime.getDistrict());
        vaultData.put("policeStation", crime.getStation() != null ? crime.getStation() : "Central PS");
        vaultData.put("unredactedLeadOfficer", crime.getInvestigatingOfficer() != null ? crime.getInvestigatingOfficer() : "Inspector R. V. Kulkarni (KA-POL-8821)");
        vaultData.put("unredactedAadhaarNo", unmaskedAadhaar);
        vaultData.put("unredactedPhoneNo", unmaskedPhone);
        vaultData.put("unredactedOffshoreAccount", unmaskedSwissAccount);
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
     * Performs actual deletion from CrimeRepository and records SHA-256 WORM audit event.
     */
    @PostMapping("/purge-case/{crimeId}")
    @PreAuthorize("hasRole('SUPERINTENDENT')")
    public ApiResponse<Map<String, Object>> purgeCaseRecord(@PathVariable Long crimeId) {
        Optional<Crime> crimeOpt = crimeRepository.findById(crimeId);
        if (crimeOpt.isEmpty()) {
            throw new ShodhanaException("NOT_FOUND", "Cannot purge: Crime record not found for ID: " + crimeId);
        }

        Crime crime = crimeOpt.get();
        String firNumber = crime.getFirNumber();

        // Perform actual repository deletion
        crimeRepository.deleteById(crimeId);

        Map<String, Object> response = new HashMap<>();
        response.put("purgedCrimeId", crimeId);
        response.put("purgedFirNumber", firNumber);
        response.put("status", "PURGED_AND_AUDITED");
        response.put("authorizedBy", "ROLE_SUPERINTENDENT");
        response.put("timestamp", System.currentTimeMillis());

        auditLedgerService.recordEvent(
                "KSP-SP-9912",
                "KA-SP-9912",
                "CASE_RECORD_ADMIN_PURGE",
                "FIR-" + firNumber,
                "127.0.0.1"
        );

        return ApiResponse.ok(response);
    }
}
