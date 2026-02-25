package com.urban.settlement.repository;

import com.urban.settlement.model.Cluster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cluster entity with hotspot queries
 */
@Repository
public interface ClusterRepository extends MongoRepository<Cluster, String> {

    /**
     * Find cluster by group ID
     */
    Optional<Cluster> findByGroupId(String groupId);

    /**
     * Find clusters by category
     */
    List<Cluster> findByCategory(String category);

    /**
     * Find clusters by ward
     */
    List<Cluster> findByWardId(String wardId);

    /**
     * Find clusters detected in date range
     */
    List<Cluster> findByDetectedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find large clusters (high issue count)
     */
    @Query("{ 'issueCount': { $gte: ?0 } }")
    List<Cluster> findLargeClusters(Integer minIssueCount);

    /**
     * Find clusters by severity level
     */
    List<Cluster> findBySeverityLevel(String severityLevel);

    /**
     * Find recent clusters (last N days)
     */
    @Query("{ 'detectedAt': { $gte: ?0 } }")
    List<Cluster> findRecentClusters(LocalDateTime since);

    /**
     * Count clusters by category
     */
    long countByCategory(String category);
}
