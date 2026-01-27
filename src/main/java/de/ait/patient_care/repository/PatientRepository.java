package de.ait.patient_care.repository;

import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.01.2026
 * Project : 09_57-59_Startup_game
 * ----------------------------------------------------------------------------
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findById(Long id);

    List<Patient>

}
