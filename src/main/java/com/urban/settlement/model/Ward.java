package com.urban.settlement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Administrative zones for spatial analysis and failure prediction
 * Represents municipal wards with boundaries and health metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wards")
public class Ward {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    /**
     * Ward boundary as GeoJSON Polygon
     * Used for spatial queries to determine which ward an issue belongs to
     */
    @GeoSpatialIndexed(type = org.springframework.data.mongodb.core.index.GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPolygon boundary;

    /**
     * Population for analytics and resource allocation
     */
    private Integer population;

    /**
     * Infrastructure health score (0-100)
     * Calculated from:
     * - Issue frequency
     * - Resolution rate
     * - Average severity
     * - Failure predictions
     * 
     * Lower scores indicate higher infrastructure degradation
     */
    private Double healthScore = 100.0;

    /**
     * Area in square kilometers
     */
    private Double areaKm2;

    /**
     * Number of assigned officers
     */
    private Integer assignedOfficerCount = 0;

    public Ward(String name, GeoJsonPolygon boundary, Integer population) {
        this.name = name;
        this.boundary = boundary;
        this.population = population;
        this.healthScore = 100.0;
    }
}
