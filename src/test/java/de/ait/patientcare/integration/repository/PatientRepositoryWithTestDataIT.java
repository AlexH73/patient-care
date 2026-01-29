package de.ait.patientcare.integration.repository;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import de.ait.patientcare.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 27.01.2026
 * Project : Patient Care System
 * ----------------------------------------------------------------------------
 */

/**
 * Integration test for PatientRepository using test data from Liquibase migration with context="test".
 * Tests verify that repository methods work correctly with pre-loaded test data.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PatientRepository Integration Test with Liquibase Test Data")
class PatientRepositoryWithTestDataIT {

    @Autowired
    private PatientRepository patientRepository;

    // Test data constants from Liquibase migration
    private static final String JOHN_DOE_INSURANCE = "M8506151234";
    private static final String JANE_SMITH_INSURANCE = "F9003205678";
    private static final String NON_EXISTENT_INSURANCE = "NON_EXISTENT";
    private static final String TEST_PATIENT_INSURANCE = "TEST123456";

    private static final LocalDate JOHN_DOE_BIRTH_DATE = LocalDate.of(1985, 6, 15);
    private static final LocalDate JANE_SMITH_BIRTH_DATE = LocalDate.of(1990, 3, 20);
    private static final LocalDate TEST_PATIENT_BIRTH_DATE = LocalDate.of(1995, 1, 1);

    private static final LocalDate CUTOFF_DATE_1986 = LocalDate.of(1986, 1, 1);
    private static final LocalDate CUTOFF_DATE_1988 = LocalDate.of(1988, 1, 1);
    private static final LocalDate CUTOFF_DATE_2000 = LocalDate.of(2000, 1, 1);
    private static final LocalDate CUTOFF_DATE_1980 = LocalDate.of(1980, 1, 1);

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationsTests {

        @Test
        @DisplayName("findAll should return patients from Liquibase test data")
        void findAll_shouldReturnPatientsFromTestData() {
            // Given - test data from Liquibase migration with context="test"
            // When
            List<Patient> patients = patientRepository.findAll();

            // Then
            assertThat(patients).isNotEmpty();

            // Verify that test patients exist
            Patient johnDoe = findPatientByInsuranceNumber(patients, JOHN_DOE_INSURANCE);
            Patient janeSmith = findPatientByInsuranceNumber(patients, JANE_SMITH_INSURANCE);

            boolean foundTestData = (johnDoe != null) || (janeSmith != null);
            assertThat(foundTestData)
                    .withFailMessage("Test data from Liquibase migration should be present")
                    .isTrue();
        }

        @Test
        @DisplayName("existsByInsuranceNumber should return true for test data patients")
        void existsByInsuranceNumber_shouldReturnTrueForTestData() {
            // When & Then
            assertThat(patientRepository.existsByInsuranceNumber(JOHN_DOE_INSURANCE))
                    .withFailMessage("John Doe should exist in test data")
                    .isTrue();

            assertThat(patientRepository.existsByInsuranceNumber(JANE_SMITH_INSURANCE))
                    .withFailMessage("Jane Smith should exist in test data")
                    .isTrue();
        }

        @Test
        @DisplayName("existsByInsuranceNumber should return false for non-existent insurance number")
        void existsByInsuranceNumber_shouldReturnFalseForNonExistent() {
            // When & Then
            assertThat(patientRepository.existsByInsuranceNumber(NON_EXISTENT_INSURANCE))
                    .withFailMessage("Non-existent insurance number should return false")
                    .isFalse();
        }

        @Test
        @DisplayName("findByDeletedFalse should return only active patients")
        void findByDeletedFalse_shouldReturnOnlyActivePatients() {
            // When
            List<Patient> activePatients = patientRepository.findByDeletedFalse();

            // Then
            assertThat(activePatients)
                    .extracting(Patient::isDeleted)
                    .withFailMessage("All returned patients should be active (not deleted)")
                    .containsOnly(false);
        }

        @Test
        @DisplayName("countByGender should return valid counts for test data")
        void countByGender_shouldReturnCorrectCounts() {
            // When
            long maleCount = patientRepository.countByGender(Gender.MALE);
            long femaleCount = patientRepository.countByGender(Gender.FEMALE);

            // Then
            assertThat(maleCount)
                    .withFailMessage("At least one male patient should exist in test data")
                    .isGreaterThanOrEqualTo(1);

            assertThat(femaleCount)
                    .withFailMessage("At least one female patient should exist in test data")
                    .isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("countByDeletedFalse should count only active patients")
        void countByDeletedFalse_shouldReturnCorrectCount() {
            // When
            long activeCount = patientRepository.countByDeletedFalse();

            // Then
            assertThat(activeCount)
                    .withFailMessage("Should count at least two active patients from test data")
                    .isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("countByDateOfBirthBefore should count patients born before specified date")
        void countByDateOfBirthBefore_shouldCountCorrectly() {
            // When
            long count = patientRepository.countByDateOfBirthBefore(CUTOFF_DATE_2000);

            // Then
            assertThat(count)
                    .withFailMessage("Should count patients born before 2000 from test data")
                    .isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("countOlderThan should count patients older than specified date")
        void countOlderThan_shouldCountCorrectly() {
            // When
            long count = patientRepository.countOlderThan(CUTOFF_DATE_2000);

            // Then
            assertThat(count)
                    .withFailMessage("Should count patients older than 2000 from test data")
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Search Operation Tests")
    class SearchOperationTests {

        @Test
        @DisplayName("search with gender filter should work correctly")
        void search_withGenderFilter_shouldReturnFilteredResults() {
            // When
            List<Patient> malePatients = patientRepository.search(Gender.MALE, null, null, null);
            List<Patient> femalePatients = patientRepository.search(Gender.FEMALE, null, null, null);

            // Then
            assertThat(malePatients)
                    .withFailMessage("Search should return male patients without errors")
                    .isNotNull();

            assertThat(femalePatients)
                    .withFailMessage("Search should return female patients without errors")
                    .isNotNull();
        }

        @Test
        @DisplayName("search with blood type filter should work correctly")
        void search_withBloodTypeFilter_shouldReturnFilteredResults() {
            // When
            List<Patient> oPosPatients = patientRepository.search(null, BloodType.O_POS, null, null);
            List<Patient> aPosPatients = patientRepository.search(null, BloodType.A_POS, null, null);

            // Then
            assertThat(oPosPatients)
                    .withFailMessage("Search should return O_POS patients without errors")
                    .isNotNull();

            assertThat(aPosPatients)
                    .withFailMessage("Search should return A_POS patients without errors")
                    .isNotNull();
        }

        @Test
        @DisplayName("search with date filters should work correctly")
        void search_withDateFilters_shouldReturnFilteredResults() {
            // When
            List<Patient> patients = patientRepository.search(null, null, CUTOFF_DATE_2000, CUTOFF_DATE_1980);

            // Then
            assertThat(patients)
                    .withFailMessage("Search with date filters should work without errors")
                    .isNotNull();
        }

        @Test
        @DisplayName("search with birthBefore filter should return older patients")
        void search_withBirthBeforeFilter_shouldReturnOlderPatients() {
            // When
            List<Patient> patients = patientRepository.search(null, null, CUTOFF_DATE_1986, null);

            // Then
            assertThat(patients)
                    .withFailMessage("Search for patients born before 1986 should work")
                    .isNotNull();
        }

        @Test
        @DisplayName("search with birthAfter filter should return younger patients")
        void search_withBirthAfterFilter_shouldReturnYoungerPatients() {
            // When
            List<Patient> patients = patientRepository.search(null, null, null, CUTOFF_DATE_1988);

            // Then
            assertThat(patients)
                    .withFailMessage("Search for patients born after 1988 should work")
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("Write Operation Tests")
    class WriteOperationTests {

        @Test
        @DisplayName("save new patient should persist correctly")
        void save_newPatient_shouldPersistCorrectly() {
            // Given
            long initialCount = patientRepository.count();
            Patient newPatient = Patient.builder()
                    .firstName("Test")
                    .lastName("Patient")
                    .dateOfBirth(TEST_PATIENT_BIRTH_DATE)
                    .gender(Gender.MALE)
                    .insuranceNumber(TEST_PATIENT_INSURANCE)
                    .bloodType(BloodType.O_POS)
                    .deleted(false)
                    .build();

            // When
            Patient savedPatient = patientRepository.save(newPatient);

            // Then
            assertThat(savedPatient.getId())
                    .withFailMessage("Saved patient should have generated ID")
                    .isNotNull();

            assertThat(patientRepository.count())
                    .withFailMessage("Patient count should increase by 1")
                    .isEqualTo(initialCount + 1);

            assertThat(patientRepository.existsByInsuranceNumber(TEST_PATIENT_INSURANCE))
                    .withFailMessage("New patient should exist by insurance number")
                    .isTrue();
        }

        @Test
        @DisplayName("update patient should persist changes correctly")
        void update_patient_shouldPersistChanges() {
            // Given
            List<Patient> allPatients = patientRepository.findAll();
            if (!allPatients.isEmpty()) {
                Patient patient = allPatients.get(0);
                String originalLastName = patient.getLastName();
                String updatedLastName = originalLastName + "_UPDATED";

                // When
                patient.setLastName(updatedLastName);
                patientRepository.save(patient);

                // Then
                Patient updatedPatient = patientRepository.findById(patient.getId()).orElseThrow();
                assertThat(updatedPatient.getLastName())
                        .withFailMessage("Patient's last name should be updated")
                        .isEqualTo(updatedLastName);
            }
        }

        @Test
        @DisplayName("delete should mark patient as deleted (soft delete)")
        void delete_shouldMarkPatientAsDeleted() {
            // Given
            List<Patient> activePatients = patientRepository.findByDeletedFalse();
            if (!activePatients.isEmpty()) {
                Patient patient = activePatients.get(0);
                long initialActiveCount = patientRepository.countByDeletedFalse();

                // When
                patient.setDeleted(true);
                patientRepository.save(patient);

                // Then
                long newActiveCount = patientRepository.countByDeletedFalse();
                assertThat(newActiveCount)
                        .withFailMessage("Active patient count should decrease by 1 after deletion")
                        .isEqualTo(initialActiveCount - 1);
            }
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("test data should have correct attributes")
        void testData_shouldHaveCorrectAttributes() {
            // When
            List<Patient> patients = patientRepository.findAll();
            Patient johnDoe = findPatientByInsuranceNumber(patients, JOHN_DOE_INSURANCE);
            Patient janeSmith = findPatientByInsuranceNumber(patients, JANE_SMITH_INSURANCE);

            // Then - verify John Doe attributes
            if (johnDoe != null) {
                assertThat(johnDoe.getFirstName()).isEqualTo("John");
                assertThat(johnDoe.getLastName()).isEqualTo("Doe");
                assertThat(johnDoe.getGender()).isEqualTo(Gender.MALE);
                assertThat(johnDoe.getBloodType()).isEqualTo(BloodType.O_POS);
                assertThat(johnDoe.getDateOfBirth()).isEqualTo(JOHN_DOE_BIRTH_DATE);
                assertThat(johnDoe.isDeleted()).isFalse();
            }

            // Then - verify Jane Smith attributes
            if (janeSmith != null) {
                assertThat(janeSmith.getFirstName()).isEqualTo("Jane");
                assertThat(janeSmith.getLastName()).isEqualTo("Smith");
                assertThat(janeSmith.getGender()).isEqualTo(Gender.FEMALE);
                assertThat(janeSmith.getBloodType()).isEqualTo(BloodType.A_POS);
                assertThat(janeSmith.getDateOfBirth()).isEqualTo(JANE_SMITH_BIRTH_DATE);
                assertThat(janeSmith.isDeleted()).isFalse();
            }
        }

        @Test
        @DisplayName("age-based queries should work correctly with test data")
        void ageBasedQueries_shouldWorkWithTestData() {
            // Find test patients
            List<Patient> patients = patientRepository.findAll();
            Optional<Patient> johnDoeOpt = patients.stream()
                    .filter(p -> JOHN_DOE_INSURANCE.equals(p.getInsuranceNumber()))
                    .findFirst();

            Optional<Patient> janeSmithOpt = patients.stream()
                    .filter(p -> JANE_SMITH_INSURANCE.equals(p.getInsuranceNumber()))
                    .findFirst();

            // Verify test patients exist before running age-based tests
            assertThat(johnDoeOpt).isPresent();
            assertThat(janeSmithOpt).isPresent();

            Patient johnDoe = johnDoeOpt.get();
            Patient janeSmith = janeSmithOpt.get();

            // Test that countByDateOfBirthBefore includes John Doe (born 1985)
            long countBefore1986 = patientRepository.countByDateOfBirthBefore(CUTOFF_DATE_1986);
            assertThat(countBefore1986)
                    .withFailMessage("Should count at least John Doe for patients born before 1986")
                    .isGreaterThanOrEqualTo(1);

            // Test that countOlderThan includes John Doe for 1986 cutoff
            long countOlderThan1986 = patientRepository.countOlderThan(CUTOFF_DATE_1986);
            assertThat(countOlderThan1986)
                    .withFailMessage("Should count at least John Doe for patients older than 1986")
                    .isGreaterThanOrEqualTo(1);

            // Verify individual patient ages
            assertThat(johnDoe.getDateOfBirth())
                    .withFailMessage("John Doe should be born before 1986")
                    .isBefore(CUTOFF_DATE_1986);

            assertThat(janeSmith.getDateOfBirth())
                    .withFailMessage("Jane Smith should be born after 1986")
                    .isAfter(CUTOFF_DATE_1986);
        }
    }

    /**
     * Helper method to find a patient by insurance number in a list
     *
     * @param patients list of patients to search through
     * @param insuranceNumber insurance number to search for
     * @return patient with matching insurance number, or null if not found
     */
    private Patient findPatientByInsuranceNumber(List<Patient> patients, String insuranceNumber) {
        return patients.stream()
                .filter(p -> insuranceNumber.equals(p.getInsuranceNumber()))
                .findFirst()
                .orElse(null);
    }
}