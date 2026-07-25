package com.ksp.shodhana.controller;

import com.ksp.shodhana.dto.response.ApiResponse;
import com.ksp.shodhana.model.FinancialTransaction;
import com.ksp.shodhana.service.ForecastingService;
import com.ksp.shodhana.service.SociologicalInsightsService;
import com.ksp.shodhana.util.LocalDataStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing Analytics & Intelligence endpoints:
 * 1. Sociological Crime Insights (Age & Area-Type Aggregations)
 * 2. Crime Forecasting & Early Warning Trend Detection
 * 3. Financial Crime & Transaction Link Analysis
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final SociologicalInsightsService sociologicalInsightsService;
    private final ForecastingService forecastingService;
    private final LocalDataStore localDataStore;

    public AnalyticsController(
            SociologicalInsightsService sociologicalInsightsService,
            ForecastingService forecastingService,
            LocalDataStore localDataStore) {
        this.sociologicalInsightsService = sociologicalInsightsService;
        this.forecastingService = forecastingService;
        this.localDataStore = localDataStore;
    }

    @GetMapping("/sociological")
    public ApiResponse<SociologicalInsightsService.SociologicalInsightsPayload> getSociologicalInsights(
            @RequestParam(required = false) String district) {
        return ApiResponse.ok(sociologicalInsightsService.getInsights(district));
    }

    @GetMapping("/forecast")
    public ApiResponse<ForecastingService.ForecastPayload> getForecast(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String crimeType) {
        return ApiResponse.ok(forecastingService.getForecast(district, crimeType));
    }

    @GetMapping("/financial")
    public ApiResponse<List<FinancialTransaction>> getFinancialTransactions(
            @RequestParam(required = false) Long criminalId,
            @RequestParam(required = false) Boolean flaggedOnly) {
        List<FinancialTransaction> txns = localDataStore.getFinancialTransactions();
        if (criminalId != null) {
            txns = txns.stream().filter(t -> t.getLinkedCriminalId() != null && t.getLinkedCriminalId().equals(criminalId)).toList();
        }
        if (Boolean.TRUE.equals(flaggedOnly)) {
            txns = txns.stream().filter(t -> Boolean.TRUE.equals(t.getIsFlagged())).toList();
        }
        return ApiResponse.ok(txns);
    }
}
