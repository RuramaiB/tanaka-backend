package com.urban.settlement.service;

import com.urban.settlement.model.Ward;
import com.urban.settlement.repository.WardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Ward management
 * Handles zone administration and health score tracking
 */
@Service
public class WardService {

    private static final Logger logger = LoggerFactory.getLogger(WardService.class);

    @Autowired
    private WardRepository wardRepository;

    /**
     * Create new ward
     */
    public Ward createWard(Ward ward) {
        Ward saved = wardRepository.save(ward);
        logger.info("Created ward: {}", saved.getId());
        return saved;
    }

    /**
     * Get ward by ID
     */
    public Ward getWardById(String id) {
        return wardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + id));
    }

    /**
     * Get all wards
     */
    public List<Ward> getAllWards() {
        return wardRepository.findAll();
    }

    /**
     * Get ward by name
     */
    public Ward getWardByName(String name) {
        return wardRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + name));
    }

    /**
     * Get wards with low health scores
     */
    public List<Ward> getWardsWithLowHealth(double threshold) {
        return wardRepository.findByHealthScoreLessThan(threshold);
    }

    /**
     * Update ward health score
     */
    public Ward updateHealthScore(String wardId, double healthScore) {
        Ward ward = getWardById(wardId);
        ward.setHealthScore(healthScore);
        Ward updated = wardRepository.save(ward);
        logger.info("Updated ward {} health score to {}", wardId, healthScore);
        return updated;
    }

    /**
     * Update ward
     */
    public Ward updateWard(String id, Ward updatedWard) {
        Ward existing = getWardById(id);

        if (updatedWard.getName() != null) {
            existing.setName(updatedWard.getName());
        }
        if (updatedWard.getPopulation() != null) {
            existing.setPopulation(updatedWard.getPopulation());
        }
        if (updatedWard.getBoundary() != null) {
            existing.setBoundary(updatedWard.getBoundary());
        }

        return wardRepository.save(existing);
    }

    /**
     * Delete ward
     */
    public void deleteWard(String id) {
        wardRepository.deleteById(id);
        logger.info("Deleted ward: {}", id);
    }
}
