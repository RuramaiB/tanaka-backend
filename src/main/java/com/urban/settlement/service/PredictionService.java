package com.urban.settlement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urban.settlement.model.Issue;
import com.urban.settlement.model.Prediction;
import com.urban.settlement.model.Ward;
import com.urban.settlement.model.enums.IssueStatus;
import com.urban.settlement.model.enums.RiskLevel;
import com.urban.settlement.repository.IssueRepository;
import com.urban.settlement.repository.PredictionRepository;
import com.urban.settlement.repository.WardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Infrastructure failure prediction service using ML models
 * 
 * Objective 3: Infrastructure failure prediction (30-90 days forecast)
 * - Analyzes historical data
 * - Predicts failure probability
 * - Generates health scores and risk alerts
 * 
 * Algorithm: Random Forest Classifier
 * Target Accuracy: 85%+
 */
@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);
    private static final String PYTHON_SCRIPT_PATH = "ml_scripts/predict_failures.py";
    private static final int HISTORICAL_MONTHS = 12;

    @Autowired
    private PythonExecutorService pythonExecutor;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Predict failure risk for a specific ward
     * 
     * @param wardId Ward identifier
     * @return Prediction result
     * @throws IOException if prediction fails
     */
    public PredictionDTO predictFailureRisk(String wardId) throws IOException {

        logger.info("Predicting failure risk for ward: {}", wardId);

        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + wardId));

        // 1. Fetch historical data (last 12 months)
        LocalDateTime startDate = LocalDateTime.now().minusMonths(HISTORICAL_MONTHS);
        List<Issue> historicalIssues = issueRepository.findByWardIdAndReportedAtBetween(
                wardId, startDate, LocalDateTime.now());

        if (historicalIssues.isEmpty()) {
            logger.warn("No historical data for ward: {}", wardId);
            return createDefaultPrediction(ward);
        }

        // 2. Prepare time-series data
        String timeSeriesJson = prepareTimeSeriesData(wardId, historicalIssues);

        // 3. Execute prediction script
        JsonNode result = pythonExecutor.executePythonScriptWithInput(
                PYTHON_SCRIPT_PATH,
                timeSeriesJson);

        // 4. Parse prediction results
        PredictionDTO prediction = parsePrediction(result, wardId, ward.getName());

        // 5. Save prediction to database
        savePrediction(prediction);

        // 6. Update ward health score
        ward.setHealthScore(prediction.getHealthScore());
        wardRepository.save(ward);

        logger.info("Prediction completed for ward {}: Health Score = {}, Risk = {}",
                wardId, prediction.getHealthScore(), prediction.getRiskLevel());

        return prediction;
    }

    /**
     * Generate predictions for all wards
     */
    public List<PredictionDTO> predictAllWards() throws IOException {
        List<Ward> wards = wardRepository.findAll();
        List<PredictionDTO> predictions = new ArrayList<>();

        for (Ward ward : wards) {
            try {
                PredictionDTO prediction = predictFailureRisk(ward.getId());
                predictions.add(prediction);
            } catch (Exception e) {
                logger.error("Failed to predict for ward: {}", ward.getId(), e);
            }
        }

        return predictions;
    }

    /**
     * Prepare time-series data for ML model
     */
    private String prepareTimeSeriesData(String wardId, List<Issue> issues) throws IOException {

        // Group issues by week
        Map<String, WeeklyData> weeklyDataMap = new HashMap<>();

        for (Issue issue : issues) {
            LocalDateTime reportedAt = issue.getReportedAt();
            String weekKey = reportedAt.getYear() + "-W" +
                    String.format("%02d", getWeekOfYear(reportedAt));

            WeeklyData weekData = weeklyDataMap.computeIfAbsent(
                    weekKey, k -> new WeeklyData());

            weekData.issueCount++;
            weekData.totalSeverity += getSeverityScore(issue.getSeverity());

            if (issue.getStatus() == IssueStatus.RESOLVED && issue.getResolvedAt() != null) {
                long resolutionTime = ChronoUnit.HOURS.between(
                        issue.getReportedAt(), issue.getResolvedAt());
                weekData.totalResolutionTime += resolutionTime;
                weekData.resolvedCount++;
            }
        }

        // Build JSON
        ObjectNode root = objectMapper.createObjectNode();
        root.put("wardId", wardId);

        ArrayNode timeSeriesArray = objectMapper.createArrayNode();
        for (Map.Entry<String, WeeklyData> entry : weeklyDataMap.entrySet()) {
            WeeklyData data = entry.getValue();

            ObjectNode weekNode = objectMapper.createObjectNode();
            weekNode.put("week", entry.getKey());
            weekNode.put("issueCount", data.issueCount);
            weekNode.put("avgSeverity", data.issueCount > 0
                    ? (double) data.totalSeverity / data.issueCount
                    : 0.0);
            weekNode.put("avgResolutionTime", data.resolvedCount > 0
                    ? (double) data.totalResolutionTime / data.resolvedCount
                    : 0.0);
            weekNode.put("resolutionRate", data.issueCount > 0
                    ? (double) data.resolvedCount / data.issueCount
                    : 0.0);

            timeSeriesArray.add(weekNode);
        }

        root.set("timeSeries", timeSeriesArray);

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Parse prediction from Python output
     */
    private PredictionDTO parsePrediction(JsonNode result, String wardId, String wardName) {
        double healthScore = result.get("healthScore").asDouble();
        double failureProbability = result.get("failureProbability").asDouble();
        String riskLevelStr = result.get("riskLevel").asText();
        String predictedDateStr = result.has("predictedFailureDate")
                ? result.get("predictedFailureDate").asText()
                : null;

        RiskLevel riskLevel = RiskLevel.valueOf(riskLevelStr);
        LocalDate predictedFailureDate = predictedDateStr != null
                ? LocalDate.parse(predictedDateStr)
                : null;

        return new PredictionDTO(
                wardId,
                wardName,
                healthScore,
                failureProbability,
                riskLevel,
                predictedFailureDate);
    }

    /**
     * Save prediction to database
     */
    private void savePrediction(PredictionDTO dto) {
        Prediction prediction = new Prediction(
                dto.getWardId(),
                "ALL", // Category (can be refined per category)
                dto.getHealthScore(),
                dto.getFailureProbability(),
                dto.getRiskLevel());

        if (dto.getPredictedFailureDate() != null) {
            prediction.setPredictedFailureDate(dto.getPredictedFailureDate());
        }

        prediction.setModelVersion("1.0");
        prediction.setConfidence(0.85); // Model confidence

        predictionRepository.save(prediction);
    }

    /**
     * Create default prediction when no historical data
     */
    private PredictionDTO createDefaultPrediction(Ward ward) {
        return new PredictionDTO(
                ward.getId(),
                ward.getName(),
                100.0, // Perfect health (no issues)
                0.0, // No failure risk
                RiskLevel.LOW,
                null);
    }

    /**
     * Get numeric severity score
     */
    private int getSeverityScore(com.urban.settlement.model.enums.Severity severity) {
        if (severity == null)
            return 1;
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    /**
     * Get week of year
     */
    private int getWeekOfYear(LocalDateTime date) {
        return (date.getDayOfYear() - 1) / 7 + 1;
    }

    /**
     * Weekly data aggregation helper
     */
    private static class WeeklyData {
        int issueCount = 0;
        int totalSeverity = 0;
        long totalResolutionTime = 0;
        int resolvedCount = 0;
    }

    /**
     * Prediction DTO
     */
    public static class PredictionDTO {
        private final String wardId;
        private final String wardName;
        private final double healthScore;
        private final double failureProbability;
        private final RiskLevel riskLevel;
        private final LocalDate predictedFailureDate;

        public PredictionDTO(String wardId, String wardName, double healthScore,
                double failureProbability, RiskLevel riskLevel,
                LocalDate predictedFailureDate) {
            this.wardId = wardId;
            this.wardName = wardName;
            this.healthScore = healthScore;
            this.failureProbability = failureProbability;
            this.riskLevel = riskLevel;
            this.predictedFailureDate = predictedFailureDate;
        }

        public String getWardId() {
            return wardId;
        }

        public String getWardName() {
            return wardName;
        }

        public double getHealthScore() {
            return healthScore;
        }

        public double getFailureProbability() {
            return failureProbability;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public LocalDate getPredictedFailureDate() {
            return predictedFailureDate;
        }
    }

    /**
     * Find ward by ID and reported date range
     */
    private List<Issue> findByWardIdAndReportedAtBetween(String wardId,
            LocalDateTime start,
            LocalDateTime end) {
        return issueRepository.findByReportedAtBetween(start, end).stream()
                .filter(issue -> wardId.equals(issue.getWardId()))
                .toList();
    }
}
