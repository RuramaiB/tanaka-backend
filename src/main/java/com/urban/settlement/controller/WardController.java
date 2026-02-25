package com.urban.settlement.controller;

import com.urban.settlement.model.Ward;
import com.urban.settlement.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Ward management
 */
@RestController
@RequestMapping("/api/wards")
@CrossOrigin(origins = "*")
public class WardController {

    @Autowired
    private WardService wardService;

    @PostMapping
    public ResponseEntity<Ward> createWard(@RequestBody Ward ward) {
        Ward created = wardService.createWard(ward);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Ward>> getAllWards() {
        List<Ward> wards = wardService.getAllWards();
        return ResponseEntity.ok(wards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ward> getWardById(@PathVariable String id) {
        Ward ward = wardService.getWardById(id);
        return ResponseEntity.ok(ward);
    }

    @GetMapping("/low-health")
    public ResponseEntity<List<Ward>> getWardsWithLowHealth(
            @RequestParam(defaultValue = "50.0") double threshold) {
        List<Ward> wards = wardService.getWardsWithLowHealth(threshold);
        return ResponseEntity.ok(wards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ward> updateWard(
            @PathVariable String id,
            @RequestBody Ward ward) {
        Ward updated = wardService.updateWard(id, ward);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWard(@PathVariable String id) {
        wardService.deleteWard(id);
        return ResponseEntity.noContent().build();
    }
}
