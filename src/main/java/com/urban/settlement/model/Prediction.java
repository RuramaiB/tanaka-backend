package com.urban.settlement.model;

import com.urban.settlement.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Infrastructure failure predictions using ML models
 * Forecasts failure probability for 30-90 day window
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "predictions")
@CompoundIndexes({
        @CompoundIndex(name = "ward_category_generated_idx", def = "{'wardId': 1, 'category': 1, 'generatedAt': -1}")
})
public class Prediction {

    @Id
    private String id;

    /**
     * Reference to Ward entity
     */
    @Indexed
    private String wardId;

    /**
     * Infrastructure category being predicted
     */
    @Indexed
    private String category;

    /**
     * Infrastructure health score (0-100)
     * 100 = perfect health, 0 = critical failure
     * 
     * Calculated from:
     * - Issue frequency trend
     * - Resolution time trend
     * - Severity distribution
     */
    private Double healthScore;

    /**
     * Probability of infrastructure failure (0.0 - 1.0)
     * Predicted by Random Forest or LSTM model
     */
    private Double failureProbability;

    /**
     * Risk level classification
     */
    @Indexed
    private RiskLevel riskLevel;

    /**
     * Predicted failure date (if failure probability > threshold)
     */
    private LocalDate predictedFailureDate;

    /**
     * When prediction was generated
     */
    @Indexed
    private LocalDateTime generatedAt;

    /**
     * ML model version for tracking
     */
    private String modelVersion;

    /**
     * Model confidence score (0.0 - 1.0)
     */
    private Double confidence;

    /**
     * Recommended preventive actions
     */
    private String recommendations;

    public Prediction(String wardId, String category, Double healthScore,
            Double failureProbability, RiskLevel riskLevel) {
        this.wardId = wardId;
        this.category = category;
        this.healthScore = healthScore;
        this.failureProbability = failureProbability;
        this.riskLevel = riskLevel;
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Calculate predicted failure date based on current trends
     */
    public void calculateFailureDate(int daysFromNow) {
        this.predictedFailureDate = LocalDate.now().plusDays(daysFromNow);
    }
}
