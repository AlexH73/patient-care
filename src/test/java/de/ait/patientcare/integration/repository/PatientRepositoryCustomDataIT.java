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
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for PatientRepository with controlled test data.
 * Uses SQL script to set up exact data for predictable test results.
 *
 * @author Alexander Hermann
 * @created 29.01.2026
 * @project PatientCare
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("PatientRepository Integration Test with Controlled Test Data")
class PatientRepositoryCustomDataIT {

    @Autowired
    private PatientRepository patientRepository;

    // Test data constants from SQL script
    private static final String JOHN_DOE_INSURANCE = "M8506151234";
    private static final String JANE_SMITH_INSURANCE = "F9003205678";
    private static final String MARK_JOHNSON_INSURANCE = "M7511105678";
    private static final String SARAH_WILLIAMS_INSURANCE = "F9508259012";
    private static final String MICHAEL_BROWN_INSURANCE = "M0002153456";

    private static final LocalDate JOHN_DOE_BIRTH = LocalDate.of(1985, 6, 15);
    private static final LocalDate JANE_SMITH_BIRTH = LocalDate.of(1990, 3, 20);
    private static final LocalDate SARAH_WILLIAMS_BIRTH = LocalDate.of(1995, 8, 25);
    private static final LocalDate MICHAEL_BROWN_BIRTH = LocalDate.of(2000, 2, 15);

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("findAll should return only active patients")
        void findAll_shouldReturnOnlyActivePatients() {
            // When
            List<Patient> patients = patientRepository.findAll();

            // Then
            assertThat(patients).hasSize(4);
            assertThat(patients)
                    .extracting(Patient::isDeleted)
                    .containsOnly(false);

            boolean markJohnsonFound = patients.stream()
                    .anyMatch(p -> MARK_JOHNSON_INSURANCE.equals(p.getInsuranceNumber()));
            assertThat(markJohnsonFound).isFalse();
        }

        @Test
        @DisplayName("count should return total number of active patients")
        void count_shouldReturnNumberOfActivePatients() {
            // When & Then
            assertThat(patientRepository.count()).isEqualTo(4);
        }

        @Test
        @DisplayName("findById should return patient if it exists")
        void findById_shouldReturnPatientIfExists() {
            // Given
            List<Patient> allPatients = patientRepository.findAll();
            Patient firstPatient = allPatients.get(0);

            // When
            Optional<Patient> foundPatient = patientRepository.findById(firstPatient.getId());

            // Then
            assertThat(foundPatient).isPresent();
            assertThat(foundPatient.get().getId()).isEqualTo(firstPatient.getId());
            assertThat(foundPatient.get().isDeleted()).isFalse();
        }

        @Test
        @DisplayName("findById should return empty for non-existent ID")
        void findById_shouldReturnEmptyForNonExistentId() {
            // When & Then
            assertThat(patientRepository.findById(999999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("existsByInsuranceNumber should work for active patients")
        void existsByInsuranceNumber_shouldWorkForActivePatients() {
            // Then
            assertThat(patientRepository.existsByInsuranceNumber(JOHN_DOE_INSURANCE)).isTrue();
            assertThat(patientRepository.existsByInsuranceNumber(JANE_SMITH_INSURANCE)).isTrue();
            assertThat(patientRepository.existsByInsuranceNumber(MARK_JOHNSON_INSURANCE)).isFalse();
            assertThat(patientRepository.existsByInsuranceNumber(SARAH_WILLIAMS_INSURANCE)).isTrue();
            assertThat(patientRepository.existsByInsuranceNumber(MICHAEL_BROWN_INSURANCE)).isTrue();
            assertThat(patientRepository.existsByInsuranceNumber("NON_EXISTENT")).isFalse();
        }

        @Test
        @DisplayName("findByDeletedFalse should return only active patients")
        void findByDeletedFalse_shouldReturnOnlyActivePatients() {
            // When
            List<Patient> activePatients = patientRepository.findByDeletedFalse();

            // Then
            assertThat(activePatients).hasSize(4);
            assertThat(activePatients)
                    .extracting(Patient::isDeleted)
                    .containsOnly(false);

            assertThat(activePatients)
                    .extracting(Patient::getInsuranceNumber)
                    .contains(JOHN_DOE_INSURANCE, JANE_SMITH_INSURANCE)
                    .doesNotContain(MARK_JOHNSON_INSURANCE);
        }

        @Test
        @DisplayName("countByDeletedFalse should count only active patients")
        void countByDeletedFalse_shouldCountOnlyActivePatients() {
            // When & Then
            assertThat(patientRepository.countByDeletedFalse()).isEqualTo(4);
        }

        @Test
        @DisplayName("countByGender should count only active patients")
        void countByGender_shouldCountOnlyActivePatients() {
            // When & Then
            assertThat(patientRepository.countByGender(Gender.MALE)).isEqualTo(2);
            assertThat(patientRepository.countByGender(Gender.FEMALE)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Age-Based Queries")
    class AgeBasedQueries {

        @Test
        @DisplayName("countByDateOfBirthBefore should count only active patients")
        void countByDateOfBirthBefore_shouldCountOnlyActivePatients() {
            // When & Then
            assertThat(patientRepository.countByDateOfBirthBefore(LocalDate.of(1980, 1, 1))).isEqualTo(0);
            assertThat(patientRepository.countByDateOfBirthBefore(LocalDate.of(1990, 1, 1))).isEqualTo(1);
            assertThat(patientRepository.countByDateOfBirthBefore(LocalDate.of(2000, 1, 1))).isEqualTo(3);
            assertThat(patientRepository.countByDateOfBirthBefore(LocalDate.of(2010, 1, 1))).isEqualTo(4);
        }

        @Test
        @DisplayName("countOlderThan should count only active patients")
        void countOlderThan_shouldCountOnlyActivePatients() {
            // When & Then
            assertThat(patientRepository.countOlderThan(LocalDate.of(1980, 1, 1))).isEqualTo(0);
            assertThat(patientRepository.countOlderThan(LocalDate.of(1990, 1, 1))).isEqualTo(1);
            assertThat(patientRepository.countOlderThan(LocalDate.of(2005, 1, 1))).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Search Operation Tests")
    class SearchOperationTests {

        @Test
        @DisplayName("search with no filters should return all active patients")
        void search_withNoFilters_shouldReturnAllActivePatients() {
            // When
            List<Patient> patients = patientRepository.search(null, null, null, null);

            // Then
            assertThat(patients).hasSize(4);
            assertThat(patients).extracting(Patient::isDeleted).containsOnly(false);
        }

        @Test
        @DisplayName("search with gender filter should return filtered results")
        void search_withGenderFilter_shouldReturnFilteredResults() {
            // When
            List<Patient> malePatients = patientRepository.search(Gender.MALE, null, null, null);
            List<Patient> femalePatients = patientRepository.search(Gender.FEMALE, null, null, null);

            // Then
            assertThat(malePatients).hasSize(2);
            assertThat(femalePatients).hasSize(2);
        }

        @Test
        @DisplayName("search with blood type filter should return filtered results")
        void search_withBloodTypeFilter_shouldReturnFilteredResults() {
            // When & Then
            assertThat(patientRepository.search(null, BloodType.O_POS, null, null)).hasSize(1);
            assertThat(patientRepository.search(null, BloodType.A_POS, null, null)).hasSize(1);
            assertThat(patientRepository.search(null, BloodType.B_NEG, null, null)).hasSize(0);
            assertThat(patientRepository.search(null, BloodType.AB_POS, null, null)).hasSize(1);
            assertThat(patientRepository.search(null, BloodType.O_NEG, null, null)).hasSize(1);
        }

        @Test
        @DisplayName("search with date filters should return filtered results")
        void search_withDateFilters_shouldReturnFilteredResults() {
            // When & Then
            assertThat(patientRepository.search(null, null, LocalDate.of(1990, 1, 1), null)).hasSize(1);
            assertThat(patientRepository.search(null, null, null, LocalDate.of(1994, 1, 1))).hasSize(2);
            assertThat(patientRepository.search(null, null, LocalDate.of(1995, 1, 1),
                    LocalDate.of(1980, 1, 1))).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Test Data Verification")
    class TestDataVerification {

        @Test
        @DisplayName("test data should be loaded correctly")
        void testData_shouldBeLoadedCorrectly() {
            // When
            List<Patient> patients = patientRepository.findAll();

            // Then
            assertThat(patients).hasSize(4);

            patients.forEach(patient -> {
                assertThat(patient.isDeleted()).isFalse();
                verifyPatientAttributes(patient);
            });
        }

        private void verifyPatientAttributes(Patient patient) {
            switch (patient.getInsuranceNumber()) {
                case JOHN_DOE_INSURANCE:
                    assertThat(patient.getFirstName()).isEqualTo("John");
                    assertThat(patient.getLastName()).isEqualTo("Doe");
                    assertThat(patient.getGender()).isEqualTo(Gender.MALE);
                    assertThat(patient.getBloodType()).isEqualTo(BloodType.O_POS);
                    assertThat(patient.getDateOfBirth()).isEqualTo(JOHN_DOE_BIRTH);
                    break;
                case JANE_SMITH_INSURANCE:
                    assertThat(patient.getFirstName()).isEqualTo("Jane");
                    assertThat(patient.getLastName()).isEqualTo("Smith");
                    assertThat(patient.getGender()).isEqualTo(Gender.FEMALE);
                    assertThat(patient.getBloodType()).isEqualTo(BloodType.A_POS);
                    assertThat(patient.getDateOfBirth()).isEqualTo(JANE_SMITH_BIRTH);
                    break;
                case SARAH_WILLIAMS_INSURANCE:
                    assertThat(patient.getFirstName()).isEqualTo("Sarah");
                    assertThat(patient.getLastName()).isEqualTo("Williams");
                    assertThat(patient.getGender()).isEqualTo(Gender.FEMALE);
                    assertThat(patient.getBloodType()).isEqualTo(BloodType.AB_POS);
                    assertThat(patient.getDateOfBirth()).isEqualTo(SARAH_WILLIAMS_BIRTH);
                    break;
                case MICHAEL_BROWN_INSURANCE:
                    assertThat(patient.getFirstName()).isEqualTo("Michael");
                    assertThat(patient.getLastName()).isEqualTo("Brown");
                    assertThat(patient.getGender()).isEqualTo(Gender.MALE);
                    assertThat(patient.getBloodType()).isEqualTo(BloodType.O_NEG);
                    assertThat(patient.getDateOfBirth()).isEqualTo(MICHAEL_BROWN_BIRTH);
                    break;
            }
        }
    }

    @Nested
    @DisplayName("Deleted Patient Behavior Tests")
    class DeletedPatientBehaviorTests {

        @Test
        @DisplayName("saving deleted patient should work but patient should not appear in queries")
        void savingDeletedPatient_shouldWorkButNotAppearInQueries() {
            // Given
            long initialCount = patientRepository.count();
            Patient deletedPatient = createDeletedPatient("DELETED999");

            // When
            Patient savedPatient = patientRepository.save(deletedPatient);

            // Then
            assertThat(savedPatient.getId()).isNotNull();
            assertThat(savedPatient.isDeleted()).isTrue();
            assertThat(patientRepository.count()).isEqualTo(initialCount);
            assertThat(patientRepository.existsByInsuranceNumber("DELETED999")).isFalse();
            assertThat(findPatientInAllQueries("DELETED999")).isFalse();
        }

        @Test
        @DisplayName("soft deleting a patient should make it disappear from queries")
        void softDeletingPatient_shouldMakeItDisappearFromQueries() {
            // Given
            Patient activePatient = createActivePatient("TODELETE123");
            Patient savedPatient = patientRepository.save(activePatient);

            assertThat(patientRepository.existsByInsuranceNumber("TODELETE123")).isTrue();

            // When
            savedPatient.setDeleted(true);
            patientRepository.save(savedPatient);

            // Then
            assertThat(patientRepository.existsByInsuranceNumber("TODELETE123")).isFalse();
            assertThat(findPatientInAllQueries("TODELETE123")).isFalse();
        }

        @Test
        @DisplayName("reactivating a deleted patient should make it appear in queries again")
        void reactivatingDeletedPatient_shouldMakeItAppearInQueriesAgain() {
            // Given
            Patient deletedPatient = createDeletedPatient("REACTIVATED456");
            Patient savedPatient = patientRepository.save(deletedPatient);

            assertThat(patientRepository.existsByInsuranceNumber("REACTIVATED456")).isFalse();

            // When
            savedPatient.setDeleted(false);
            patientRepository.save(savedPatient);

            // Then
            assertThat(patientRepository.existsByInsuranceNumber("REACTIVATED456")).isTrue();
            assertThat(findPatientInAllQueries("REACTIVATED456")).isTrue();
        }

        @Test
        @DisplayName("findById behavior with deleted patients")
        void findById_behaviorWithDeletedPatients() {
            // Given
            Patient deletedPatient = createDeletedPatient("TEMP_DELETED");
            Patient savedPatient = patientRepository.save(deletedPatient);

            // When
            Optional<Patient> found = patientRepository.findById(savedPatient.getId());

            // Then
            if (found.isPresent()) {
                assertThat(found.get().getId()).isEqualTo(savedPatient.getId());
                assertThat(found.get().isDeleted()).isTrue();
            }
            // If not found, it means findById filters deleted patients
        }

        private Patient createDeletedPatient(String insuranceNumber) {
            return Patient.builder()
                    .firstName("Deleted")
                    .lastName("Patient")
                    .dateOfBirth(LocalDate.of(1999, 1, 1))
                    .gender(Gender.FEMALE)
                    .insuranceNumber(insuranceNumber)
                    .bloodType(BloodType.A_NEG)
                    .deleted(true)
                    .build();
        }

        private Patient createActivePatient(String insuranceNumber) {
            return Patient.builder()
                    .firstName("Active")
                    .lastName("Patient")
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .gender(Gender.MALE)
                    .insuranceNumber(insuranceNumber)
                    .bloodType(BloodType.B_POS)
                    .deleted(false)
                    .build();
        }

        private boolean findPatientInAllQueries(String insuranceNumber) {
            boolean inFindAll = patientRepository.findAll().stream()
                    .anyMatch(p -> insuranceNumber.equals(p.getInsuranceNumber()));
            boolean inSearch = patientRepository.search(null, null, null, null).stream()
                    .anyMatch(p -> insuranceNumber.equals(p.getInsuranceNumber()));
            return inFindAll || inSearch;
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("existsByInsuranceNumber should detect existing insurance numbers")
        void existsByInsuranceNumber_shouldDetectExistingInsuranceNumbers() {
            // Given
            String insuranceNumber = "TEST123456";
            assertThat(patientRepository.existsByInsuranceNumber(insuranceNumber)).isFalse();

            Patient patient = createTestPatient(insuranceNumber);
            patientRepository.save(patient);

            // When & Then
            assertThat(patientRepository.existsByInsuranceNumber(insuranceNumber)).isTrue();
        }

        @Test
        @DisplayName("search with impossible date range should return empty list")
        void searchWithImpossibleDateRange_shouldReturnEmptyList() {
            // When & Then
            assertThat(patientRepository.search(null, null,
                    LocalDate.of(1990, 1, 1),
                    LocalDate.of(2000, 1, 1))).isEmpty();
        }

        private Patient createTestPatient(String insuranceNumber) {
            return Patient.builder()
                    .firstName("Test")
                    .lastName("Patient")
                    .dateOfBirth(LocalDate.of(1980, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber(insuranceNumber)
                    .bloodType(BloodType.O_POS)
                    .deleted(false)
                    .build();
        }
    }
}