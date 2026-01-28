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

@DisplayName("Patient entity validation tests")
class PatientTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Patient.PatientBuilder basePatient() {
        return Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .insuranceNumber("INS123456")
                .bloodType(BloodType.O_POS);
    }

    @Test
    @DisplayName("Valid patient should produce no violations")
    void validPatient_shouldHaveNoViolations() {
        Patient patient = basePatient().build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("Name validation")
    class NameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Blank first name should produce violation")
        void firstNameBlank_shouldHaveViolation(String firstName) {
            Patient patient = basePatient()
                    .firstName(firstName)
                    .build();

            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is mandatory");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Blank last name should produce violation")
        void lastNameBlank_shouldHaveViolation(String lastName) {
            Patient patient = basePatient()
                    .lastName(lastName)
                    .build();

            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is mandatory");
        }
    }

    @Test
    @DisplayName("Future date of birth should produce violation")
    void futureDateOfBirth_shouldHaveViolation() {
        Patient patient = basePatient()
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Date of birth must be in the past");
    }

    @Test
    @DisplayName("Missing gender should produce violation")
    void nullGender_shouldHaveViolation() {
        Patient patient = basePatient()
                .gender(null)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Gender is mandatory");
    }

    @Test
    @DisplayName("Missing blood type should produce violation")
    void nullBloodType_shouldHaveViolation() {
        Patient patient = basePatient()
                .bloodType(null)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Blood type is mandatory");
    }

    @Test
    @DisplayName("Blank insurance number should produce violation")
    void blankInsuranceNumber_shouldHaveViolation() {
        Patient patient = basePatient()
                .insuranceNumber("   ")
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Insurance number is mandatory");
    }

    @Test
    @DisplayName("Multiple invalid fields should produce all violations")
    void patientWithMultipleViolations_shouldShowAllErrors() {
        Patient patient = Patient.builder()
                .firstName("")
                .lastName("")
                .dateOfBirth(LocalDate.now().plusDays(1))
                .gender(null)
                .insuranceNumber("")
                .bloodType(null)
                .build();

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

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
    @DisplayName("Default values should be correct")
    void patientWithDefaultValues_shouldBeValid() {
        Patient patient = basePatient().build();

        assertThat(validator.validate(patient)).isEmpty();
        assertThat(patient.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("createdAt should be null on creation")
    void createdAt_shouldBeNull_whenObjectCreated() {
        Patient patient = basePatient().build();

        assertThat(patient.getCreatedAt()).isNull();
    }
}

