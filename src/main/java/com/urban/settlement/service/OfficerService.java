package com.urban.settlement.service;

import com.urban.settlement.model.Officer;
import com.urban.settlement.model.enums.AvailabilityStatus;
import com.urban.settlement.repository.OfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Officer management
 * Handles availability and workload tracking
 */
@Service
public class OfficerService {

    private static final Logger logger = LoggerFactory.getLogger(OfficerService.class);

    @Autowired
    private OfficerRepository officerRepository;

    /**
     * Create new officer
     */
    public Officer createOfficer(Officer officer) {
        Officer saved = officerRepository.save(officer);
        logger.info("Created officer: {}", saved.getId());
        return saved;
    }

    /**
     * Get officer by ID
     */
    public Officer getOfficerById(String id) {
        return officerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found: " + id));
    }

    /**
     * Get all officers
     */
    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    /**
     * Get available officers
     */
    public List<Officer> getAvailableOfficers() {
        return officerRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
    }

    /**
     * Get officers by skill
     */
    public List<Officer> getOfficersBySkill(String skill) {
        return officerRepository.findBySkillsContaining(skill);
    }

    /**
     * Update officer availability
     */
    public Officer updateAvailability(String officerId, AvailabilityStatus status) {
        Officer officer = getOfficerById(officerId);
        officer.setAvailabilityStatus(status);
        Officer updated = officerRepository.save(officer);
        logger.info("Updated officer {} availability to {}", officerId, status);
        return updated;
    }

    /**
     * Update officer
     */
    public Officer updateOfficer(String id, Officer updatedOfficer) {
        Officer existing = getOfficerById(id);

        if (updatedOfficer.getName() != null) {
            existing.setName(updatedOfficer.getName());
        }
        if (updatedOfficer.getEmail() != null) {
            existing.setEmail(updatedOfficer.getEmail());
        }
        if (updatedOfficer.getSkills() != null) {
            existing.setSkills(updatedOfficer.getSkills());
        }
        if (updatedOfficer.getCurrentLocation() != null) {
            existing.setCurrentLocation(updatedOfficer.getCurrentLocation());
        }

        return officerRepository.save(existing);
    }

    /**
     * Delete officer
     */
    public void deleteOfficer(String id) {
        officerRepository.deleteById(id);
        logger.info("Deleted officer: {}", id);
    }

    /**
     * Increment officer workload
     */
    public void incrementWorkload(String officerId) {
        Officer officer = getOfficerById(officerId);
        officer.incrementWorkload();
        officerRepository.save(officer);
    }

    /**
     * Decrement officer workload
     */
    public void decrementWorkload(String officerId) {
        Officer officer = getOfficerById(officerId);
        officer.decrementWorkload();
        officerRepository.save(officer);
    }
}
