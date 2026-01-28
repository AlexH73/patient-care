package de.ait.patient_care.controller;

import de.ait.patient_care.entity.Patient;
import de.ait.patient_care.enums.BloodType;
import de.ait.patient_care.enums.Gender;
import de.ait.patient_care.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Patient Management API")
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

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
        return ResponseEntity.status(201).body(patientService.create(patient));
    }

    @Operation(summary = "Update existing patient")
    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id,
                                          @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @Operation(summary = "Soft delete patient")
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
            @RequestParam(required = false) Integer ageTo
    ) {
        return ResponseEntity.ok(patientService.search(gender, bloodType, ageFrom, ageTo));
    }

    @Operation(summary = "Get patient statistics")
    @GetMapping("/statistics")
    public ResponseEntity<Object> statistics() {
        return ResponseEntity.ok(patientService.statistics());
    }
}

