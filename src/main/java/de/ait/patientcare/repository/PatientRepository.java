package de.ait.patientcare.repository;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.01.2026
 * Project : Patient Care System
 * ----------------------------------------------------------------------------
 */
@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    boolean existsByInsuranceNumber(String insuranceNumber);

    // To search for active patients
    List<Patient> findByDeletedFalse();

    // For statistics
    long countByDeletedFalse();
    long countByGender(Gender gender);

    // To search for patients over a certain age
    long countByDateOfBirthBefore(LocalDate date);

    // Custom search method with filters
    @Query("SELECT p FROM Patient p WHERE p.deleted = false " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:bloodType IS NULL OR p.bloodType = :bloodType) " +
            "AND (:birthBefore IS NULL OR p.dateOfBirth <= :birthBefore) " +
            "AND (:birthAfter IS NULL OR p.dateOfBirth >= :birthAfter)")
    List<Patient> search(@Param("gender") Gender gender,
                         @Param("bloodType") BloodType bloodType,
                         @Param("birthBefore") LocalDate birthBefore,
                         @Param("birthAfter") LocalDate birthAfter);

    // Alternative method for countOlderThan
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.deleted = false AND p.dateOfBirth < :date")
    long countOlderThan(@Param("date") LocalDate date);
}