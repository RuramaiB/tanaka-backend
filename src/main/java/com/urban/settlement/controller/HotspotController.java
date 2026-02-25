package com.urban.settlement.controller;

import com.urban.settlement.model.Issue;
import com.urban.settlement.service.ClusteringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for hotspot detection and clustering
 * 
 * Objective 2: Hotspot detection & clustering
 * 
 * Endpoints:
 * - GET /api/hotspots - Detect and return hotspots
 * - GET /api/hotspots/duplicates/{id} - Find duplicate issues
 */
@RestController
@RequestMapping("/api/hotspots")
@CrossOrigin(origins = "*")
public class HotspotController {

    @Autowired
    private ClusteringService clusteringService;

    /**
     * GET /api/hotspots
     * Detect hotspots using DBSCAN clustering
     * 
     * Returns cluster data for heatmap visualization
     */
    @GetMapping
    public ResponseEntity<?> detectHotspots() {
        try {
            List<ClusteringService.ClusterDTO> clusters = clusteringService.detectHotspots();

            Map<String, Object> response = new HashMap<>();
            response.put("clusters", clusters);
            response.put("count", clusters.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Hotspot detection failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/hotspots/duplicates/{id}
     * Find duplicate issues (same cluster)
     */
    @GetMapping("/duplicates/{id}")
    public ResponseEntity<List<Issue>> findDuplicates(@PathVariable String id) {
        List<Issue> duplicates = clusteringService.findDuplicates(id);
        return ResponseEntity.ok(duplicates);
    }
}
