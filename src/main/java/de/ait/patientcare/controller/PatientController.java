package de.ait.patientcare.controller;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import de.ait.patientcare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Patient Management API")
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    @Value("${app.clinic.name:PatientCare Clinic}")
    private String clinicName;

    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Welcome to " + clinicName + "!");
    }

    @Operation(summary = "Get all patients")
    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @Operation(summary = "Get patient by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(patientService.getPatientById(id));
        } catch (RuntimeException e) {
            log.warn("Patient not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new patient")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Patient patient) {
        try {
            Patient saved = patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Patient creation failed (duplicate insurance): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Insurance number must be unique"));
        } catch (Exception e) {
            log.warn("Patient creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Update existing patient by ID")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Patient updated) {
        try {
            Patient patient = patientService.updatePatient(id, updated);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            log.warn("Patient not found for update: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.warn("Patient update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Soft delete patient by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Patient not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search patients by filters")
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) BloodType bloodType,
            @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo) {

        List<Patient> patients = patientService.searchPatients(gender, bloodType, ageFrom, ageTo);
        return ResponseEntity.ok(patients);
    }

    @Operation(summary = "Get patient statistics")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(patientService.getStatistics());
    }
}