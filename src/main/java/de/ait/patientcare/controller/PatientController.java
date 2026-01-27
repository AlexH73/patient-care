package de.ait.patientcare.controller;


import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.BloodType;
import de.ait.patientcare.entity.Gender;
import de.ait.patientcare.repository.PatientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Patient Management API")
@RestController
@RequestMapping("/api/patients")
@Slf4j
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Value("${app.clinic.name:PatientCare Clinic}")
    private String clinicName;

    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Welcome to " + clinicName + "!");
    }

    // ---------- GET ALL ----------

    @Operation(summary = "Get all patients")
    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(patientRepository.findByDeletedFalse());
    }

    // ---------- GET BY ID ----------

    @Operation(summary = "Get patient by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- POST ----------

    @Operation(summary = "Create new patient")
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody Patient patient) {
        try {
            Patient saved = patientRepository.save(patient);
            log.info("Patient created with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.warn("Patient creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---------- PUT ----------

    @Operation(summary = "Update existing patient by ID")
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @Valid @RequestBody Patient updated) {
        Optional<Patient> optional = patientRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient existing = optional.get();

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setGender(updated.getGender());
        existing.setBloodType(updated.getBloodType());
        existing.setInsuranceNumber(updated.getInsuranceNumber());

        try {
            Patient saved = patientRepository.save(existing);
            log.info("Patient updated with ID: {}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.warn("Patient update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---------- DELETE ----------

    @Operation(summary = "Soft delete patient by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Patient patient = optional.get();
        patient.setDeleted(true);
        patientRepository.save(patient);
        log.info("Patient soft-deleted with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ---------- SEARCH ----------

    @Operation(summary = "Search patients by filters")
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) BloodType bloodType,
            @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo
    ) {
        LocalDate today = LocalDate.now();
        LocalDate birthBefore = (ageTo != null) ? today.minusYears(ageTo) : null;
        LocalDate birthAfter = (ageFrom != null) ? today.minusYears(ageFrom) : null;

        log.info("Patient search with filters: gender={}, bloodType={}, ageFrom={}, ageTo={}",
                gender, bloodType, ageFrom, ageTo);

        return ResponseEntity.ok(
                patientRepository.search(gender, bloodType, birthBefore, birthAfter)
        );
    }

    // ---------- STATISTICS ----------

    @Operation(summary = "Get patient statistics")
    @GetMapping("/statistics")
    public ResponseEntity<Object> statistics() {
        long total = patientRepository.countByDeletedFalse();
        long male = patientRepository.countByGender(Gender.MALE);
        long female = patientRepository.countByGender(Gender.FEMALE);
        long other = patientRepository.countByGender(Gender.OTHER);
        long olderThan60 = patientRepository.countOlderThan(LocalDate.now().minusYears(60));

        log.info("Statistics requested");

        return ResponseEntity.ok(Map.of(
                "totalPatients", total,
                "maleCount", male,
                "femaleCount", female,
                "otherCount", other,
                "olderThan60", olderThan60
        ));
    }
}
