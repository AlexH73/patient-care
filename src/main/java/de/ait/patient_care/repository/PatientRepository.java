package de.ait.patient_care.repository;

import de.ait.patient_care.entity.Patient;
import de.ait.patient_care.enums.BloodType;
import de.ait.patient_care.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByDeletedFalse();

    long countByDeletedFalse();
    long countByGender(Gender gender);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.dateOfBirth <= :date AND p.deleted = false")
    long countOlderThan(LocalDate date);

    @Query("""
            SELECT p FROM Patient p
            WHERE p.deleted = false
              AND (:gender IS NULL OR p.gender = :gender)
              AND (:bloodType IS NULL OR p.bloodType = :bloodType)
              AND (:birthBefore IS NULL OR p.dateOfBirth <= :birthBefore)
              AND (:birthAfter IS NULL OR p.dateOfBirth >= :birthAfter)
            """)
    List<Patient> search(Gender gender,
                         BloodType bloodType,
                         LocalDate birthBefore,
                         LocalDate birthAfter);
}


