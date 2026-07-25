package com.ksp.shodhana.service;

import com.ksp.shodhana.model.Crime;
import com.ksp.shodhana.model.Criminal;
import com.ksp.shodhana.util.LocalDataStore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Sociological Crime Insights aggregation.
 * Performs demographic & area-type aggregations over existing dataset.
 */
@Service
public class SociologicalInsightsService {

    private static final Logger log = LoggerFactory.getLogger(SociologicalInsightsService.class);
    private final LocalDataStore localDataStore;

    public SociologicalInsightsService(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    public SociologicalInsightsPayload getInsights(String district) {
        log.info("Computing sociological crime insights for district filter: {}", district);

        List<Criminal> criminals = localDataStore.getCriminals();
        List<Crime> crimes = localDataStore.getCrimes();

        if (district != null && !district.isBlank()) {
            criminals = criminals.stream()
                    .filter(c -> c.getDistrict() != null && c.getDistrict().equalsIgnoreCase(district))
                    .collect(Collectors.toList());
            crimes = crimes.stream()
                    .filter(c -> c.getDistrict() != null && c.getDistrict().equalsIgnoreCase(district))
                    .collect(Collectors.toList());
        }

        // Age Group Aggregation
        Map<String, Integer> ageGroupCounts = new LinkedHashMap<>();
        ageGroupCounts.put("18-25 Yrs", 0);
        ageGroupCounts.put("26-35 Yrs", 0);
        ageGroupCounts.put("36-50 Yrs", 0);
        ageGroupCounts.put("50+ Yrs", 0);

        for (Criminal c : criminals) {
            Integer age = c.getAge() != null ? c.getAge() : c.getAgeYear();
            if (age != null) {
                if (age <= 25) ageGroupCounts.put("18-25 Yrs", ageGroupCounts.get("18-25 Yrs") + 1);
                else if (age <= 35) ageGroupCounts.put("26-35 Yrs", ageGroupCounts.get("26-35 Yrs") + 1);
                else if (age <= 50) ageGroupCounts.put("36-50 Yrs", ageGroupCounts.get("36-50 Yrs") + 1);
                else ageGroupCounts.put("50+ Yrs", ageGroupCounts.get("50+ Yrs") + 1);
            }
        }

        // Area Type Aggregation
        Map<String, Integer> areaTypeCounts = new LinkedHashMap<>();
        areaTypeCounts.put("Urban", 0);
        areaTypeCounts.put("Semi-Urban", 0);
        areaTypeCounts.put("Rural", 0);

        for (Crime c : crimes) {
            String area = c.getAreaType();
            if (area == null) area = "urban";
            if ("urban".equalsIgnoreCase(area)) areaTypeCounts.put("Urban", areaTypeCounts.get("Urban") + 1);
            else if ("semi-urban".equalsIgnoreCase(area)) areaTypeCounts.put("Semi-Urban", areaTypeCounts.get("Semi-Urban") + 1);
            else areaTypeCounts.put("Rural", areaTypeCounts.get("Rural") + 1);
        }

        return SociologicalInsightsPayload.builder()
                .targetDistrict(district != null ? district : "All Karnataka State")
                .totalCriminalsAnalyzed(criminals.size())
                .totalCrimesAnalyzed(crimes.size())
                .ageDistribution(ageGroupCounts)
                .areaTypeDistribution(areaTypeCounts)
                .methodologyDisclaimer("Foundational implementation, scoped for hackathon timeline — reflects statistical distribution within the current seed dataset.")
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SociologicalInsightsPayload {
        private String targetDistrict;
        private int totalCriminalsAnalyzed;
        private int totalCrimesAnalyzed;
        private Map<String, Integer> ageDistribution;
        private Map<String, Integer> areaTypeDistribution;
        private String methodologyDisclaimer;
    }
}
