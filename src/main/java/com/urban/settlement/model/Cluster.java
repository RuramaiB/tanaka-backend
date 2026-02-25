package com.urban.settlement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotspot detection results from DBSCAN clustering
 * Groups similar nearby issues for duplicate detection and resource
 * optimization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clusters")
public class Cluster {

    @Id
    private String id;

    /**
     * Unique cluster identifier
     * Assigned to all issues in this cluster via Issue.groupId
     */
    @Indexed(unique = true)
    private String groupId;

    /**
     * Cluster centroid (center point)
     * Calculated as average of all issue locations in cluster
     */
    @GeoSpatialIndexed(type = org.springframework.data.mongodb.core.index.GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint center;

    /**
     * List of issue IDs in this cluster
     */
    private List<String> issueIds = new ArrayList<>();

    /**
     * Dominant category in cluster
     */
    @Indexed
    private String category;

    /**
     * Dominant severity level
     */
    private String severityLevel;

    /**
     * Number of issues in cluster
     */
    private Integer issueCount;

    /**
     * Cluster radius in meters
     */
    private Double radiusMeters;

    /**
     * When cluster was detected
     */
    @Indexed
    private LocalDateTime detectedAt;

    /**
     * Average description similarity score (0-1)
     * Used for duplicate detection
     */
    private Double avgSimilarity;

    /**
     * Ward this cluster belongs to
     */
    private String wardId;

    public Cluster(String groupId, GeoJsonPoint center, List<String> issueIds,
            String category, String severityLevel) {
        this.groupId = groupId;
        this.center = center;
        this.issueIds = issueIds;
        this.category = category;
        this.severityLevel = severityLevel;
        this.issueCount = issueIds.size();
        this.detectedAt = LocalDateTime.now();
    }
}
