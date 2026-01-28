package de.ait.patientcare.service;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.enums.BloodType;
import de.ait.patientcare.enums.Gender;
import de.ait.patientcare.handler.NotFoundException;
import de.ait.patientcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 28.01.2026
 * Project : PatientCare
 * ----------------------------------------------------------------------------
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> getAll() {
        log.debug("Fetching all non-deleted patients");
        return patientRepository.findByDeletedFalse();
    }

    public Patient getById(Long id) {
        log.debug("Fetching patient with id={}", id);

        return patientRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> {
                    log.warn("Patient not found with id={}", id);
                    return new NotFoundException("Patient not found");

                });
    }

    public Patient create(Patient patient) {
        log.info("Creating patient: {} {}", patient.getFirstName(), patient.getLastName());
        return patientRepository.save(patient);
    }

    public Patient update(Long id, Patient updated) {
        log.info("Updating patient with id={}", id);

        if (updated == null) {
            throw new IllegalArgumentException("Updated patient must not be null");
        }

        Patient existing = getById(id);

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setGender(updated.getGender());
        existing.setBloodType(updated.getBloodType());
        existing.setInsuranceNumber(updated.getInsuranceNumber());

        return patientRepository.save(existing);
    }

    public void softDelete(Long id) {
        log.info("Soft deleting patient with id={}", id);
        Patient patient = getById(id);
        patient.setDeleted(true);
        patientRepository.save(patient);
    }

    public List<Patient> search(Gender gender, BloodType bloodType, Integer ageFrom, Integer ageTo) {
        log.debug("Searching patients gender={}, bloodType={}, ageFrom={}, ageTo={}",
                gender, bloodType, ageFrom, ageTo);

        LocalDate today = LocalDate.now();
        LocalDate birthBefore = ageTo != null ? today.minusYears(ageTo) : null;
        LocalDate birthAfter = ageFrom != null ? today.minusYears(ageFrom) : null;

        return patientRepository.search(gender, bloodType, birthBefore, birthAfter);
    }

    public Map<String, Object> statistics() {
        log.info("Generating patient statistics");

        return Map.of(
                "totalPatients", patientRepository.countByDeletedFalse(),
                "maleCount", patientRepository.countByGender(Gender.MALE),
                "femaleCount", patientRepository.countByGender(Gender.FEMALE),
                "otherCount", patientRepository.countByGender(Gender.OTHER),
                "olderThan60", patientRepository.countOlderThan(LocalDate.now().minusYears(60))
        );
    }
}
