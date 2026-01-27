package de.ait.patient_care.repository;

import de.ait.patient_care.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.01.2026
 * Project : 09_57-59_Startup_game
 * ----------------------------------------------------------------------------
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findById(Long id);


}
