package de.ait.patientcare.entity;

import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 28.01.2026
 * Project : PatientCare
 * ----------------------------------------------------------------------------
 */
public class PatientTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Creating a valid patient - no violations")
    void validPatient_shouldHaveNoViolations() {
        // Given
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS)
                .build();

        // When
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        // Then
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("First and last name validation")
    class NameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("An empty name is a violation")
        void firstNameBlank_shouldHaveViolation(String firstName) {
            Patient patient = Patient.builder()
                    .firstName(firstName)
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is mandatory");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("An empty last name is a violation")
        void lastNameBlank_shouldHaveViolation(String lastName) {
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName(lastName)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is mandatory");
        }
    }

    @Test
    @DisplayName("Date of birth in the future is a violation")
    void futureDateOfBirth_shouldHaveViolation() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.now().plusDays(1)) // Tomorrow!
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Date of birth must be in the past");
    }

    @Test
    @DisplayName("Lack of gender is a violation")
    void nullGender_shouldHaveViolation() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(null) // Не указан пол
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Gender is mandatory");
    }

    @Test
    @DisplayName("Lack of blood type is a violation")
    void nullBloodType_shouldHaveViolation() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(null) // Не указана группа крови
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Blood type is mandatory");
    }

    @Test
    @DisplayName("An empty insurance number is a violation")
    void blankInsuranceNumber_shouldHaveViolation() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("   ") // Только пробелы
                .bloodType(BloodType.O_POS)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Insurance number is mandatory");
    }

    @Test
    @DisplayName("Multiple violations - show all errors")
    void patientWithMultipleViolations_shouldShowAllErrors() {
        Patient patient = Patient.builder()
                .firstName("") // Empty name
                .lastName("")  // Empty last name
                .dateOfBirth(LocalDate.now().plusDays(1)) // Future date
                .gender(null) // Empty gender
                .insuranceNumber("") // Empty insurance number
                .bloodType(null) // Empty blood type
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations).hasSize(6); // Все 6 полей невалидны

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "First name is mandatory",
                        "Last name is mandatory",
                        "Date of birth must be in the past",
                        "Gender is mandatory",
                        "Insurance number is mandatory",
                        "Blood type is mandatory"
                );
    }

    @Test
    @DisplayName("Checking default values")
    void patientWithDefaultValues_shouldBeValid() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS)
                .build();

        // Checking the validation
        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);
        assertThat(violations).isEmpty();

        // Checking the default values
        assertThat(patient.isDeleted()).isFalse(); // deleted = false
    }

    @Test
    @DisplayName("createdAt should be null when the object is created")
    void createdAt_shouldBeNull_whenObjectCreated() {
        Patient patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS)
                .build();

        assertThat(patient.getCreatedAt()).isNull();
    }
}
