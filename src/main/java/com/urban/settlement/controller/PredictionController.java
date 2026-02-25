package com.urban.settlement.controller;

import com.urban.settlement.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for infrastructure failure predictions
 * 
 * Objective 3: Infrastructure failure prediction (30-90 days forecast)
 * 
 * Endpoints:
 * - GET /api/predictions/failure-risk - Get failure risk predictions
 * - GET /api/predictions/ward/{wardId} - Get prediction for specific ward
 * - POST /api/predictions/generate - Generate predictions for all wards
 */
@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    /**
     * GET /api/predictions/failure-risk
     * Get failure risk predictions for all wards
     */
    @GetMapping("/failure-risk")
    public ResponseEntity<?> getFailureRiskPredictions(
            @RequestParam(required = false) String wardId) {

        try {
            if (wardId != null) {
                // Single ward prediction
                PredictionService.PredictionDTO prediction = predictionService.predictFailureRisk(wardId);
                return ResponseEntity.ok(prediction);
            } else {
                // All wards
                List<PredictionService.PredictionDTO> predictions = predictionService.predictAllWards();

                Map<String, Object> response = new HashMap<>();
                response.put("predictions", predictions);
                response.put("count", predictions.size());

                return ResponseEntity.ok(response);
            }

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Prediction failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/predictions/ward/{wardId}
     * Get prediction for specific ward
     */
    @GetMapping("/ward/{wardId}")
    public ResponseEntity<?> getPredictionForWard(@PathVariable String wardId) {
        try {
            PredictionService.PredictionDTO prediction = predictionService.predictFailureRisk(wardId);
            return ResponseEntity.ok(prediction);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Prediction failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/predictions/generate
     * Generate predictions for all wards (async operation)
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generatePredictions() {
        try {
            List<PredictionService.PredictionDTO> predictions = predictionService.predictAllWards();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Predictions generated successfully");
            response.put("count", predictions.size());
            response.put("predictions", predictions);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Prediction generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
