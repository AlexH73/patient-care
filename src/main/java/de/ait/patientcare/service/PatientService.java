package de.ait.patientcare.service;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import de.ait.patientcare.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        log.info("Fetching all patients");
        return patientRepository.findByDeletedFalse();
    }

    public Patient getPatientById(Long id) {
        log.info("Fetching patient by ID: {}", id);
        return patientRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> {
                    log.warn("Patient not found with ID: {}", id);
                    return new RuntimeException("Patient not found");
                });
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        log.info("Creating new patient: {} {}",
                patient.getFirstName(), patient.getLastName());

        // Проверка уникальности номера страховки
        if (patientRepository.existsByInsuranceNumber(patient.getInsuranceNumber())) {
            log.warn("Duplicate insurance number: {}", patient.getInsuranceNumber());
            throw new DataIntegrityViolationException("Insurance number must be unique");
        }

        Patient saved = patientRepository.save(patient);
        log.info("Patient created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Patient updatePatient(Long id, Patient patientDetails) {
        log.info("Updating patient with ID: {}", id);

        Patient patient = getPatientById(id);

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setGender(patientDetails.getGender());
        patient.setInsuranceNumber(patientDetails.getInsuranceNumber());
        patient.setBloodType(patientDetails.getBloodType());

        log.info("Patient updated successfully: ID {}", id);
        return patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        log.info("Soft-deleting patient with ID: {}", id);
        Patient patient = getPatientById(id);
        patient.setDeleted(true);
        patientRepository.save(patient);
        log.info("Patient soft-deleted: ID {}", id);
    }

    public List<Patient> searchPatients(Gender gender, BloodType bloodType,
                                        Integer ageFrom, Integer ageTo) {
        log.info("Searching patients with filters: gender={}, bloodType={}, ageFrom={}, ageTo={}",
                gender, bloodType, ageFrom, ageTo);

        LocalDate today = LocalDate.now();
        LocalDate birthBefore = (ageTo != null) ? today.minusYears(ageTo) : null;
        LocalDate birthAfter = (ageFrom != null) ? today.minusYears(ageFrom) : null;

        return patientRepository.search(gender, bloodType, birthBefore, birthAfter);
    }

    public Map<String, Object> getStatistics() {
        log.info("Getting patient statistics");

        long total = patientRepository.countByDeletedFalse();
        long male = patientRepository.countByGender(Gender.MALE);
        long female = patientRepository.countByGender(Gender.FEMALE);
        long other = patientRepository.countByGender(Gender.OTHER);
        long olderThan60 = patientRepository.countByDateOfBirthBefore(
                LocalDate.now().minusYears(60));

        return Map.of(
                "totalPatients", total,
                "maleCount", male,
                "femaleCount", female,
                "otherCount", other,
                "olderThan60", olderThan60
        );
    }
}
