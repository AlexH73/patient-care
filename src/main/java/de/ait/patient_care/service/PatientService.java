package de.ait.patient_care.service;



import de.ait.patient_care.exception.NotFoundException;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import de.ait.patientcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> getAll() {
        return patientRepository.findByDeletedFalse();
    }

    public Patient getById(Long id) { return patientRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .orElseThrow(() -> new NotFoundException("Patient not found"));
    }


    public Patient create(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient update(Long id, Patient updated) {
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
        Patient patient = getById(id);
        patient.setDeleted(true);
        patientRepository.save(patient);
    }

    public List<Patient> search(Gender gender, BloodType bloodType, Integer ageFrom, Integer ageTo) {
        LocalDate today = LocalDate.now();
        LocalDate birthBefore = ageTo != null ? today.minusYears(ageTo) : null;
        LocalDate birthAfter = ageFrom != null ? today.minusYears(ageFrom) : null;

        return patientRepository.search(gender, bloodType, birthBefore, birthAfter);
    }

    public Object statistics() {
        return Map.of(
                "totalPatients", patientRepository.countByDeletedFalse(),
                "maleCount", patientRepository.countByGender(Gender.MALE),
                "femaleCount", patientRepository.countByGender(Gender.FEMALE),
                "otherCount", patientRepository.countByGender(Gender.OTHER),
                "olderThan60", patientRepository.countOlderThan(LocalDate.now().minusYears(60))
        );
    }
}


