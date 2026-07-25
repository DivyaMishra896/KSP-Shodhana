package com.ksp.shodhana.service;

import com.ksp.shodhana.exception.ShodhanaException;
import com.ksp.shodhana.model.Crime;
import com.ksp.shodhana.model.CrimeCriminalLink;
import com.ksp.shodhana.model.Criminal;
import com.ksp.shodhana.repository.CriminalRepository;
import com.ksp.shodhana.util.LocalDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Criminal record operations & Criminology Offender Profiling.
 * Implements explainable risk scoring and repeat-offender classification.
 */
@Service
public class CriminalService {

    private static final Logger log = LoggerFactory.getLogger(CriminalService.class);
    private final CriminalRepository criminalRepository;
    private final LocalDataStore localDataStore;

    public CriminalService(CriminalRepository criminalRepository, LocalDataStore localDataStore) {
        this.criminalRepository = criminalRepository;
        this.localDataStore = localDataStore;
    }

    public List<Criminal> findAll(String district, String riskLevel, String status, String search) {
        log.debug("Finding criminals - district: {}, risk: {}, status: {}, search: {}",
                district, riskLevel, status, search);
        List<Criminal> criminals = criminalRepository.findAll(district, riskLevel, status, search);
        return criminals.stream().map(this::enrichCriminalProfiling).collect(Collectors.toList());
    }

    public Criminal findById(Long id) {
        log.debug("Finding criminal by ID: {}", id);
        Criminal criminal = criminalRepository.findById(id)
                .orElseThrow(() -> new ShodhanaException("CRIMINAL_NOT_FOUND", "Criminal with ID " + id + " not found"));
        return enrichCriminalProfiling(criminal);
    }

    /**
     * Criminology Offender Profiling (Pillar 1: Explainable AI & Offender Risk Assessment).
     * Computes explainable risk score (0-100), repeat offender status, and plain-language explanation.
     *
     * Formula: RiskScore = min(100, (priorOffenses * 15) + (criticalCount * 20) + (highCount * 10) + (recentOffenses * 15))
     */
    public Criminal enrichCriminalProfiling(Criminal c) {
        if (c == null) return null;
        Long id = c.getRowId();

        List<CrimeCriminalLink> links = localDataStore.getLinks().stream()
                .filter(l -> l.getCriminalRowId() != null && l.getCriminalRowId().equals(id))
                .collect(Collectors.toList());

        int priorOffenseCount = links.size();
        boolean isRepeatOffender = priorOffenseCount >= 2;

        int criticalCount = 0;
        int highCount = 0;
        int recentCount = 0;

        for (CrimeCriminalLink l : links) {
            Crime crime = localDataStore.getCrimes().stream()
                    .filter(cr -> cr.getRowId() != null && cr.getRowId().equals(l.getCrimeRowId()))
                    .findFirst().orElse(null);
            if (crime != null) {
                if ("Critical".equalsIgnoreCase(crime.getSeverity())) criticalCount++;
                if ("High".equalsIgnoreCase(crime.getSeverity())) highCount++;
                if (crime.getDateOccurred() != null && (crime.getDateOccurred().startsWith("2026") || crime.getDateOccurred().startsWith("2025"))) {
                    recentCount++;
                }
            }
        }

        int riskScore = Math.min(100, (priorOffenseCount * 15) + (criticalCount * 20) + (highCount * 10) + (recentCount * 15));
        if (riskScore == 0 && c.getRiskLevel() != null) {
            if ("Critical".equalsIgnoreCase(c.getRiskLevel())) riskScore = 85;
            else if ("High".equalsIgnoreCase(c.getRiskLevel())) riskScore = 65;
            else if ("Medium".equalsIgnoreCase(c.getRiskLevel())) riskScore = 45;
            else riskScore = 25;
        }

        String explanation = String.format("Risk Score: %d/100 — %d prior offense(s) (%d Critical, %d High), %d within active 2025-2026 window",
                riskScore, priorOffenseCount, criticalCount, highCount, recentCount);

        c.setPriorOffenseCount(priorOffenseCount);
        c.setIsRepeatOffender(isRepeatOffender);
        c.setRiskScore(riskScore);
        c.setRiskExplanation(explanation);

        return c;
    }
}
