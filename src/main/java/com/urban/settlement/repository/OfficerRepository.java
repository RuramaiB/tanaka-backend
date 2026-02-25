package com.urban.settlement.repository;

import com.urban.settlement.model.Officer;
import com.urban.settlement.model.enums.AvailabilityStatus;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Officer entity with skill and workload queries
 */
@Repository
public interface OfficerRepository extends MongoRepository<Officer, String> {

    /**
     * Find officers by availability status
     * Used for task assignment
     */
    List<Officer> findByAvailabilityStatus(AvailabilityStatus status);

    /**
     * Find officers with specific skill
     * Used for skill-based task matching
     */
    List<Officer> findBySkillsContaining(String skill);

    /**
     * Find officer by ID with workload constraint
     */
    Optional<Officer> findByIdAndWorkloadLessThan(String id, Integer maxWorkload);

    /**
     * Find officers by email
     */
    Optional<Officer> findByEmail(String email);

    /**
     * Find officers near a location
     * Used for proximity-based assignment
     */
    List<Officer> findByCurrentLocationNear(Point location, Distance distance);

    /**
     * Find officers by assigned ward
     */
    List<Officer> findByAssignedWardId(String wardId);

    /**
     * Find available officers with workload below threshold
     * Used for optimization
     */
    @Query("{ 'availabilityStatus': 'AVAILABLE', 'workload': { $lt: ?0 } }")
    List<Officer> findAvailableOfficersWithWorkloadBelow(Integer maxWorkload);

    /**
     * Find officers with specific skill and availability
     */
    @Query("{ 'skills': { $in: ?0 }, 'availabilityStatus': 'AVAILABLE' }")
    List<Officer> findAvailableOfficersWithSkills(List<String> skills);

    /**
     * Find top performing officers
     */
    @Query("{ 'performanceScore': { $gte: ?0 } }")
    List<Officer> findByPerformanceScoreGreaterThanEqual(Double minScore);

    /**
     * Count available officers
     */
    long countByAvailabilityStatus(AvailabilityStatus status);
}
