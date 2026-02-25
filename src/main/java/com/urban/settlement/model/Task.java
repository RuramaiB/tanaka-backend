package com.urban.settlement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Task assignment tracking for issue resolution
 * Links issues to officers with route optimization data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
@CompoundIndexes({
        @CompoundIndex(name = "officer_completed_idx", def = "{'officerId': 1, 'completedAt': 1}"),
        @CompoundIndex(name = "issue_status_idx", def = "{'issueId': 1, 'completedAt': 1}")
})
public class Task {

    @Id
    private String id;

    /**
     * Reference to Issue entity
     */
    @Indexed
    private String issueId;

    /**
     * Reference to Officer entity
     */
    @Indexed
    private String officerId;

    @Indexed
    private LocalDateTime assignedAt;

    @Indexed
    private LocalDateTime completedAt;

    /**
     * Estimated or actual cost for resolution
     */
    private Double cost;

    /**
     * Position in optimized daily route (1-based)
     * Used by route optimization algorithm
     */
    private Integer routeOrder;

    /**
     * Estimated duration in minutes
     */
    private Integer estimatedDuration;

    /**
     * Actual duration in minutes (calculated from assignedAt to completedAt)
     */
    private Integer actualDuration;

    /**
     * Notes or comments from officer
     */
    private String notes;

    /**
     * Quality rating (1-5) for performance tracking
     */
    private Integer qualityRating;

    public Task(String issueId, String officerId) {
        this.issueId = issueId;
        this.officerId = officerId;
        this.assignedAt = LocalDateTime.now();
    }

    /**
     * Mark task as completed and calculate actual duration
     */
    public void complete() {
        this.completedAt = LocalDateTime.now();
        if (this.assignedAt != null) {
            this.actualDuration = (int) java.time.Duration.between(
                    this.assignedAt, this.completedAt).toMinutes();
        }
    }
}
