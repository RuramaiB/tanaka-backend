package com.urban.settlement.model;

import com.urban.settlement.model.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Municipal officers responsible for resolving infrastructure issues
 * Tracks skills, workload, location, and performance metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "officers")
public class Officer {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    /**
     * Link to system User entity
     */
    @Indexed
    private String userId;

    private String phoneNumber;

    /**
     * Officer skills for task matching
     * Examples: ROAD_REPAIR, DRAINAGE_MAINTENANCE, WASTE_MANAGEMENT, ELECTRICAL
     */
    private List<String> skills = new ArrayList<>();

    /**
     * Current location for proximity-based task assignment
     */
    @GeoSpatialIndexed(type = org.springframework.data.mongodb.core.index.GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint currentLocation;

    /**
     * Number of active (non-completed) tasks
     * Updated automatically when tasks are assigned/completed
     */
    @Indexed
    private Integer workload = 0;

    @Indexed
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    /**
     * Performance score (0.0 - 100.0)
     * Calculated from:
     * - Task completion rate
     * - Average resolution time
     * - Quality ratings
     */
    private Double performanceScore = 100.0;

    /**
     * Maximum tasks per day (capacity constraint for optimization)
     */
    private Integer maxTasksPerDay = 8;

    /**
     * Assigned ward/zone (optional)
     */
    private String assignedWardId;

    public Officer(String name, String email, List<String> skills) {
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.workload = 0;
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.performanceScore = 100.0;
    }

    /**
     * Increment workload when task is assigned
     */
    public void incrementWorkload() {
        this.workload++;
        if (this.workload >= this.maxTasksPerDay) {
            this.availabilityStatus = AvailabilityStatus.BUSY;
        }
    }

    /**
     * Decrement workload when task is completed
     */
    public void decrementWorkload() {
        if (this.workload > 0) {
            this.workload--;
        }
        if (this.workload < this.maxTasksPerDay) {
            this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        }
    }
}
