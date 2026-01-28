package de.ait.patientcare.controller;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.enums.BloodType;
import de.ait.patientcare.enums.Gender;
import de.ait.patientcare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management API")
public class PatientController {

    private final PatientService patientService;

    @Value("${app.clinic.name:Patient Care Clinic}")
    private String clinicName;

    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Welcome to " + clinicName + "!");
    }

    @Operation(summary = "Get all patients")
    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(patientService.getAll());
    }

    @Operation(summary = "Get patient by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @Operation(summary = "Create new patient")
    @PostMapping
    public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
        Patient saved = patientService.create(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Update existing patient by ID")
    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient updated) {
        return ResponseEntity.ok(patientService.update(id, updated));
    }

    @Operation(summary = "Soft delete patient by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search patients by filters")
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) BloodType bloodType,
            @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo) {

        return ResponseEntity.ok(patientService.search(gender, bloodType, ageFrom, ageTo));
    }

    @Operation(summary = "Get patient statistics")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        return ResponseEntity.ok(patientService.statistics());
    }
}

