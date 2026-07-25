package com.ksp.shodhana.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksp.shodhana.util.LocalDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForecastingServiceTest {

    private ForecastingService forecastingService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        LocalDataStore localDataStore = new LocalDataStore(objectMapper);
        localDataStore.init();
        forecastingService = new ForecastingService(localDataStore);
    }

    @Test
    @DisplayName("Verify different district/crimeType filters produce distinct real monthly counts")
    void testDifferentFiltersProduceDifferentMonthlyCounts() {
        // Query 1: All State (No filters)
        ForecastingService.ForecastPayload allForecast = forecastingService.getForecast(null, null);
        assertNotNull(allForecast);
        assertNotEquals("INSUFFICIENT_DATA", allForecast.getTrendDirection());
        assertTrue(allForecast.getMonthlyCounts().size() >= 2);

        // Query 2: Bengaluru Urban filter
        ForecastingService.ForecastPayload bengaluruForecast = forecastingService.getForecast("Bengaluru Urban", null);
        assertNotNull(bengaluruForecast);
        assertNotEquals("INSUFFICIENT_DATA", bengaluruForecast.getTrendDirection());

        // Assert monthlyCounts differ between All State and Bengaluru Urban
        assertNotEquals(allForecast.getMonthlyCounts(), bengaluruForecast.getMonthlyCounts(),
                "Regression assertion: All State and Bengaluru Urban must produce different monthlyCounts!");

        // Query 3: Theft filter across all districts
        ForecastingService.ForecastPayload theftForecast = forecastingService.getForecast(null, "Theft");
        assertNotNull(theftForecast);
        assertNotEquals("INSUFFICIENT_DATA", theftForecast.getTrendDirection());

        // Assert monthlyCounts differ between All State and Theft
        assertNotEquals(allForecast.getMonthlyCounts(), theftForecast.getMonthlyCounts(),
                "Regression assertion: All State and Theft filter must produce different monthlyCounts!");

        // Assert monthlyCounts differ between Bengaluru Urban and Theft
        assertNotEquals(bengaluruForecast.getMonthlyCounts(), theftForecast.getMonthlyCounts(),
                "Regression assertion: Bengaluru Urban and Theft filter must produce different monthlyCounts!");
    }

    @Test
    @DisplayName("Verify low/zero data edge cases degrade gracefully to INSUFFICIENT_DATA state")
    void testLowDataEdgeCaseDegradesGracefully() {
        // Nonexistent district -> 0 incidents
        ForecastingService.ForecastPayload zeroForecast = forecastingService.getForecast("NonexistentDistrict", null);
        assertNotNull(zeroForecast);
        assertEquals("INSUFFICIENT_DATA", zeroForecast.getTrendDirection());
        assertEquals(0.0, zeroForecast.getTrailingMovingAverage());
        assertFalse(zeroForecast.isEmergingCluster());
        assertTrue(zeroForecast.getWarningMessage().contains("INSUFFICIENT DATA"));

        // Single incident district (Mysuru) -> 1 incident (< 3 threshold)
        ForecastingService.ForecastPayload mysuruForecast = forecastingService.getForecast("Mysuru", null);
        assertNotNull(mysuruForecast);
        assertEquals("INSUFFICIENT_DATA", mysuruForecast.getTrendDirection());
        assertEquals(0.0, mysuruForecast.getTrailingMovingAverage());
        assertFalse(mysuruForecast.isEmergingCluster());
    }
}
