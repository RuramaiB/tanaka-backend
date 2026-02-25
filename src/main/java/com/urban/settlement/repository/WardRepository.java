package com.urban.settlement.repository;

import com.urban.settlement.model.Ward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Ward entity with health score and spatial queries
 */
@Repository
public interface WardRepository extends MongoRepository<Ward, String> {

    /**
     * Find ward by name
     */
    Optional<Ward> findByName(String name);

    /**
     * Find wards with health score below threshold
     * Used for identifying at-risk areas
     */
    List<Ward> findByHealthScoreLessThan(Double threshold);

    /**
     * Find wards ordered by health score (ascending - worst first)
     */
    @Query("{ }")
    List<Ward> findAllOrderByHealthScoreAsc();

    /**
     * Find wards with low officer coverage
     */
    @Query("{ 'assignedOfficerCount': { $lt: ?0 } }")
    List<Ward> findWardsWithLowOfficerCoverage(Integer minOfficers);

    /**
     * Find wards by population range
     */
    List<Ward> findByPopulationBetween(Integer minPop, Integer maxPop);
}
