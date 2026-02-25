package com.urban.settlement.repository;

import com.urban.settlement.model.Prediction;
import com.urban.settlement.model.enums.RiskLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Prediction entity with risk and forecast queries
 */
@Repository
public interface PredictionRepository extends MongoRepository<Prediction, String> {

    /**
     * Find latest prediction for a ward and category
     */
    Optional<Prediction> findFirstByWardIdAndCategoryOrderByGeneratedAtDesc(
            String wardId, String category);

    /**
     * Find all predictions for a ward
     */
    List<Prediction> findByWardId(String wardId);

    /**
     * Find predictions by risk level
     */
    List<Prediction> findByRiskLevel(RiskLevel riskLevel);

    /**
     * Find predictions by category
     */
    List<Prediction> findByCategory(String category);

    /**
     * Find predictions generated in date range
     */
    List<Prediction> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find high-risk predictions
     */
    @Query("{ 'riskLevel': { $in: ['HIGH', 'CRITICAL'] } }")
    List<Prediction> findHighRiskPredictions();

    /**
     * Find predictions with low health scores
     */
    @Query("{ 'healthScore': { $lt: ?0 } }")
    List<Prediction> findByHealthScoreLessThan(Double threshold);

    /**
     * Find predictions with high failure probability
     */
    @Query("{ 'failureProbability': { $gte: ?0 } }")
    List<Prediction> findByFailureProbabilityGreaterThanEqual(Double threshold);

    /**
     * Find latest predictions for all wards
     */
    @Query("{ 'generatedAt': { $gte: ?0 } }")
    List<Prediction> findRecentPredictions(LocalDateTime since);

    /**
     * Count predictions by risk level
     */
    long countByRiskLevel(RiskLevel riskLevel);
}
