package com.ksp.shodhana.controller;

import com.ksp.shodhana.dto.response.ApiResponse;
import com.ksp.shodhana.dto.response.WorkspacePayload;
import com.ksp.shodhana.service.GraphService;
import com.ksp.shodhana.service.NetworkService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for criminal network graph operations.
 */
@RestController
@RequestMapping("/api/v1/network")
public class NetworkController {

    private final NetworkService networkService;
    private final GraphService graphService;

    public NetworkController(NetworkService networkService, GraphService graphService) {
        this.networkService = networkService;
        this.graphService = graphService;
    }

    /** Multi-hop graph path analysis between two criminal suspects */
    @GetMapping("/path")
    public ApiResponse<Map<String, Object>> getShortestPath(
            @RequestParam Long sourceId,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "3") int maxHops) {
        Map<String, Object> pathResult = graphService.findShortestCriminalPath(sourceId, targetId, maxHops);
        return ApiResponse.ok(pathResult);
    }

    /** Get network graph centered on a specific criminal */
    @GetMapping("/{criminalId}")
    public ApiResponse<WorkspacePayload.NetworkGraphData> getNetworkByCriminal(
            @PathVariable Long criminalId,
            @RequestParam(defaultValue = "2") int depth) {
        var graphData = networkService.getNetworkByCriminal(criminalId, depth);
        return ApiResponse.ok(graphData);
    }

    /** Get network graph for all criminals linked to a crime */
    @GetMapping("/crime/{crimeId}")
    public ApiResponse<WorkspacePayload.NetworkGraphData> getNetworkByCrime(
            @PathVariable Long crimeId) {
        var graphData = networkService.getNetworkByCrime(crimeId);
        return ApiResponse.ok(graphData);
    }
}
