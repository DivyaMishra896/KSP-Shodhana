package com.ksp.shodhana.service;

import com.ksp.shodhana.model.Crime;
import com.ksp.shodhana.util.LocalDataStore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for Crime Forecasting & Early Warning Trend Detection.
 * Computes moving averages, linear trend direction, and emerging cluster flags dynamically
 * from actual dateOccurred / dateReported timestamps in filtered crime records.
 */
@Service
public class ForecastingService {

    private static final Logger log = LoggerFactory.getLogger(ForecastingService.class);
    private final LocalDataStore localDataStore;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public ForecastingService(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    public ForecastPayload getForecast(String district, String crimeType) {
        log.info("Computing crime forecast for district: {}, crimeType: {}", district, crimeType);

        List<Crime> crimes = localDataStore.getCrimes();

        if (district != null && !district.isBlank()) {
            crimes = crimes.stream()
                    .filter(c -> c.getDistrict() != null && c.getDistrict().equalsIgnoreCase(district))
                    .toList();
        }
        if (crimeType != null && !crimeType.isBlank()) {
            crimes = crimes.stream()
                    .filter(c -> c.getCrimeType() != null && c.getCrimeType().equalsIgnoreCase(crimeType))
                    .toList();
        }

        // Parse dateOccurred / dateReported into YearMonth counts
        Map<YearMonth, Integer> rawCountsByYm = new HashMap<>();
        List<YearMonth> parsedYms = new ArrayList<>();

        for (Crime c : crimes) {
            YearMonth ym = parseYearMonth(c.getDateOccurred());
            if (ym == null) {
                ym = parseYearMonth(c.getDateReported());
            }
            if (ym != null) {
                rawCountsByYm.put(ym, rawCountsByYm.getOrDefault(ym, 0) + 1);
                parsedYms.add(ym);
            }
        }

        long distinctMonthsCount = parsedYms.stream().distinct().count();

        // Edge case handling: Insufficient historical data (< 3 incidents or < 2 distinct months)
        if (parsedYms.size() < 3 || distinctMonthsCount < 2) {
            Map<String, Integer> minimalMap = new LinkedHashMap<>();
            if (!parsedYms.isEmpty()) {
                Collections.sort(parsedYms);
                for (YearMonth ym : parsedYms.stream().distinct().sorted().toList()) {
                    minimalMap.put(ym.format(DISPLAY_FORMATTER), rawCountsByYm.get(ym));
                }
            }
            return ForecastPayload.builder()
                    .targetDistrict(district != null ? district : "All Karnataka State")
                    .targetCrimeType(crimeType != null ? crimeType : "All Categories")
                    .monthlyCounts(minimalMap)
                    .trailingMovingAverage(0.0)
                    .recentPeriodCount(parsedYms.size())
                    .trendDirection("INSUFFICIENT_DATA")
                    .changePercent(0.0)
                    .isEmergingCluster(false)
                    .warningMessage("INSUFFICIENT DATA: Fewer than 3 historical crime incidents found for this filter combination to calculate moving average trend.")
                    .methodologyDisclaimer("Rule-based trend detection over historical patterns, scoped for hackathon timeline — not a trained predictive ML model.")
                    .build();
        }

        // Determine continuous date range from min YearMonth to max YearMonth
        YearMonth minYm = Collections.min(parsedYms);
        YearMonth maxYm = Collections.max(parsedYms);

        Map<String, Integer> monthlyCounts = new LinkedHashMap<>();
        YearMonth curr = minYm;
        while (!curr.isAfter(maxYm)) {
            String label = curr.format(DISPLAY_FORMATTER);
            monthlyCounts.put(label, rawCountsByYm.getOrDefault(curr, 0));
            curr = curr.plusMonths(1);
        }

        // Calculate trailing moving average (all months preceding the latest month)
        List<String> monthKeys = new ArrayList<>(monthlyCounts.keySet());
        String lastMonthKey = monthKeys.get(monthKeys.size() - 1);
        int recentPeriodCount = monthlyCounts.get(lastMonthKey);

        double trailingSum = 0;
        int trailingMonthCount = 0;
        for (int i = 0; i < monthKeys.size() - 1; i++) {
            trailingSum += monthlyCounts.get(monthKeys.get(i));
            trailingMonthCount++;
        }

        double trailingAvg = trailingMonthCount > 0 ? trailingSum / trailingMonthCount : 0.0;

        // Rule-Based Early Warning: If recent period count > 1.4x trailing average -> Emerging Cluster
        boolean isEmergingCluster = trailingAvg > 0 && recentPeriodCount > (trailingAvg * 1.4);

        String trendDirection;
        if (trailingAvg == 0) {
            trendDirection = recentPeriodCount > 0 ? "INCREASING" : "STABLE";
        } else if (recentPeriodCount > trailingAvg * 1.2) {
            trendDirection = "INCREASING";
        } else if (recentPeriodCount < trailingAvg * 0.8) {
            trendDirection = "DECREASING";
        } else {
            trendDirection = "STABLE";
        }

        double changePercent = trailingAvg > 0 ? ((recentPeriodCount - trailingAvg) / trailingAvg) * 100.0 : 0.0;

        String warningMessage = isEmergingCluster
                ? String.format("EMERGING CLUSTER WARNING: Recent incident volume (%d) exceeds trailing average (%.1f) by %+.1f%%",
                recentPeriodCount, trailingAvg, changePercent)
                : String.format("Crime trend is %s (Recent: %d vs Moving Avg: %.1f)", trendDirection.toLowerCase(), recentPeriodCount, trailingAvg);

        return ForecastPayload.builder()
                .targetDistrict(district != null ? district : "All Karnataka State")
                .targetCrimeType(crimeType != null ? crimeType : "All Categories")
                .monthlyCounts(monthlyCounts)
                .trailingMovingAverage(Math.round(trailingAvg * 10.0) / 10.0)
                .recentPeriodCount(recentPeriodCount)
                .trendDirection(trendDirection)
                .changePercent(Math.round(changePercent * 10.0) / 10.0)
                .isEmergingCluster(isEmergingCluster)
                .warningMessage(warningMessage)
                .methodologyDisclaimer("Rule-based trend detection over historical patterns, scoped for hackathon timeline — not a trained predictive ML model.")
                .build();
    }

    private YearMonth parseYearMonth(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            String cleaned = dateStr.trim();
            if (cleaned.length() >= 7) {
                int year = Integer.parseInt(cleaned.substring(0, 4));
                int month = Integer.parseInt(cleaned.substring(5, 7));
                if (month >= 1 && month <= 12) {
                    return YearMonth.of(year, month);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse YearMonth from date string '{}': {}", dateStr, e.getMessage());
        }
        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPayload {
        private String targetDistrict;
        private String targetCrimeType;
        private Map<String, Integer> monthlyCounts;
        private double trailingMovingAverage;
        private int recentPeriodCount;
        private String trendDirection;
        private double changePercent;
        private boolean isEmergingCluster;
        private String warningMessage;
        private String methodologyDisclaimer;
    }
}
