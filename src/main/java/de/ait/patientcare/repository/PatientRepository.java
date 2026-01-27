package de.ait.patientcare.repository;

import de.ait.patientcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.01.2026
 * Project : 09_57-59_Startup_game
 * ----------------------------------------------------------------------------
 */
@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    boolean existsByInsuranceNumber(String insuranceNumber);
}


}
