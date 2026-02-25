package com.urban.settlement.controller;

import com.urban.settlement.model.Officer;
import com.urban.settlement.model.enums.AvailabilityStatus;
import com.urban.settlement.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Officer management
 */
@RestController
@RequestMapping("/api/officers")
@CrossOrigin(origins = "*")
public class OfficerController {

    @Autowired
    private OfficerService officerService;

    @PostMapping
    public ResponseEntity<Officer> createOfficer(@RequestBody Officer officer) {
        Officer created = officerService.createOfficer(officer);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Officer>> getAllOfficers() {
        List<Officer> officers = officerService.getAllOfficers();
        return ResponseEntity.ok(officers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Officer> getOfficerById(@PathVariable String id) {
        Officer officer = officerService.getOfficerById(id);
        return ResponseEntity.ok(officer);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Officer>> getAvailableOfficers() {
        List<Officer> officers = officerService.getAvailableOfficers();
        return ResponseEntity.ok(officers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Officer> updateOfficer(
            @PathVariable String id,
            @RequestBody Officer officer) {
        Officer updated = officerService.updateOfficer(id, officer);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<Officer> updateAvailability(
            @PathVariable String id,
            @RequestParam AvailabilityStatus status) {
        Officer updated = officerService.updateAvailability(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficer(@PathVariable String id) {
        officerService.deleteOfficer(id);
        return ResponseEntity.noContent().build();
    }
}
