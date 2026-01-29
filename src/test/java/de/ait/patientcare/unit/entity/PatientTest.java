package de.ait.patientcare.unit.entity;

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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Patient Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Create patient with builder - success")
        void createPatient_withBuilder_success() {
            // Given
            LocalDate birthDate = LocalDate.of(1990, 1, 1);
            LocalDateTime createdAt = LocalDateTime.now();

            // When
            Patient patient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(birthDate)
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(createdAt)
                    .version(1L)
                    .deleted(false)
                    .build();

            // Then
            assertThat(patient).isNotNull();
            assertThat(patient.getId()).isEqualTo(1L);
            assertThat(patient.getFirstName()).isEqualTo("John");
            assertThat(patient.getLastName()).isEqualTo("Doe");
            assertThat(patient.getDateOfBirth()).isEqualTo(birthDate);
            assertThat(patient.getGender()).isEqualTo(Gender.MALE);
            assertThat(patient.getInsuranceNumber()).isEqualTo("INS123456");
            assertThat(patient.getBloodType()).isEqualTo(BloodType.O_POS);
            assertThat(patient.getCreatedAt()).isEqualTo(createdAt);
            assertThat(patient.getVersion()).isEqualTo(1L);
            assertThat(patient.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Create patient with minimal builder - sets defaults")
        void createPatient_withMinimalBuilder_setsDefaults() {
            // When
            Patient patient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // Then - проверяем только обязательные поля и дефолтные значения
            assertThat(patient).isNotNull();
            assertThat(patient.getFirstName()).isEqualTo("John");
            assertThat(patient.getLastName()).isEqualTo("Doe");
            assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
            assertThat(patient.getGender()).isEqualTo(Gender.MALE);
            assertThat(patient.getInsuranceNumber()).isEqualTo("INS123456");
            assertThat(patient.getBloodType()).isEqualTo(BloodType.O_POS);
            assertThat(patient.getId()).isNotNull();
            assertThat(patient.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Use toBuilder to create modified copy")
        void toBuilder_createsModifiedCopy() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now();
            Patient original = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(createdAt)
                    .deleted(false)
                    .build();

            // When
            Patient modified = original.toBuilder()
                    .firstName("Jane")
                    .gender(Gender.FEMALE)
                    .bloodType(BloodType.A_POS)
                    .deleted(true)
                    .build();

            // Then
            assertThat(modified.getId()).isEqualTo(1L);
            assertThat(modified.getFirstName()).isEqualTo("Jane");
            assertThat(modified.getLastName()).isEqualTo("Doe");
            assertThat(modified.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(modified.getBloodType()).isEqualTo(BloodType.A_POS);
            assertThat(modified.isDeleted()).isTrue();
            assertThat(modified.getCreatedAt()).isEqualTo(createdAt);

            // Original should remain unchanged
            assertThat(original.getFirstName()).isEqualTo("John");
            assertThat(original.getGender()).isEqualTo(Gender.MALE);
            assertThat(original.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Default values are set correctly")
        void defaultValues_areSetCorrectly() {
            // When
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // Then
            assertThat(patient.getVersion()).isEqualTo(0L);
            assertThat(patient.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid patient - no validation errors")
        void validPatient_noValidationErrors() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("First name is mandatory")
        void firstName_mandatory(String firstName) {
            // Given
            Patient patient = Patient.builder()
                    .firstName(firstName)
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("First name is mandatory");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Last name is mandatory")
        void lastName_mandatory(String lastName) {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName(lastName)
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Last name is mandatory");
        }

        @Test
        @DisplayName("Date of birth is mandatory")
        void dateOfBirth_mandatory() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(null)
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Date of birth is mandatory");
        }

        @Test
        @DisplayName("Date of birth must be in the past")
        void dateOfBirth_mustBeInPast() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().plusDays(1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Gender is mandatory")
        void gender_mandatory() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(null)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Gender is mandatory");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Insurance number is mandatory")
        void insuranceNumber_mandatory(String insuranceNumber) {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber(insuranceNumber)
                    .bloodType(BloodType.O_POS)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Insurance number is mandatory");
        }

        @Test
        @DisplayName("Blood type is mandatory")
        void bloodType_mandatory() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(null)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Blood type is mandatory");
        }

        @Test
        @DisplayName("Multiple validation errors - all reported")
        void multipleValidationErrors_allReported() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("")
                    .lastName("")
                    .dateOfBirth(null)
                    .gender(null)
                    .insuranceNumber("")
                    .bloodType(null)
                    .build();

            // When
            Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

            // Then
            assertThat(violations).hasSize(6);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains(
                            "First name is mandatory",
                            "Last name is mandatory",
                            "Date of birth is mandatory",
                            "Gender is mandatory",
                            "Insurance number is mandatory",
                            "Blood type is mandatory"
                    );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Two patients with same ID are equal")
        void patientsWithSameId_areEqual() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Patient patient1 = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(now)
                    .version(1L)
                    .deleted(false)
                    .build();

            Patient patient2 = Patient.builder()
                    .id(1L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1995, 5, 5))
                    .gender(Gender.FEMALE)
                    .insuranceNumber("INS654321")
                    .bloodType(BloodType.A_POS)
                    .createdAt(now)
                    .version(2L)
                    .deleted(true)
                    .build();

            // Then
            assertThat(patient1).isEqualTo(patient2);
            assertThat(patient1.hashCode()).isEqualTo(patient2.hashCode());
        }

        @Test
        @DisplayName("Two patients with different IDs are not equal")
        void patientsWithDifferentIds_areNotEqual() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Patient patient1 = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(now)
                    .version(0L)
                    .deleted(false)
                    .build();

            Patient patient2 = Patient.builder()
                    .id(2L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(now)
                    .version(0L)
                    .deleted(false)
                    .build();

            // Then
            assertThat(patient1).isNotEqualTo(patient2);
            assertThat(patient1.hashCode()).isNotEqualTo(patient2.hashCode());
        }

        @Test
        @DisplayName("Patients with same ID but different createdAt are equal")
        void patientsWithSameIdButDifferentCreatedAt_areEqual() {
            // Given
            Patient patient1 = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(LocalDateTime.now())
                    .version(0L)
                    .deleted(false)
                    .build();

            Patient patient2 = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(LocalDateTime.now().plusSeconds(1))
                    .version(0L)
                    .deleted(false)
                    .build();

            assertThat(patient1).isEqualTo(patient2);
            assertThat(patient1.hashCode()).isEqualTo(patient2.hashCode());
        }

        @Test
        @DisplayName("Patient is not equal to null")
        void patient_notEqualToNull() {
            // Given
            Patient patient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(LocalDateTime.now())
                    .version(0L)
                    .deleted(false)
                    .build();

            // Then
            assertThat(patient).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Patient is not equal to object of different type")
        void patient_notEqualToDifferentType() {
            // Given
            Patient patient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(LocalDateTime.now())
                    .version(0L)
                    .deleted(false)
                    .build();

            // Then
            assertThat(patient).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Reflexive: patient equals itself")
        void patient_equalsItself() {
            // Given
            Patient patient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(LocalDateTime.now())
                    .version(0L)
                    .deleted(false)
                    .build();

            // Then
            assertThat(patient).isEqualTo(patient);
        }

        @Test
        @DisplayName("Symmetric: if a equals b then b equals a")
        void equals_isSymmetric() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Patient patient1 = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(now)
                    .version(0L)
                    .deleted(false)
                    .build();

            Patient patient2 = Patient.builder()
                    .id(1L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1995, 5, 5))
                    .gender(Gender.FEMALE)
                    .insuranceNumber("INS654321")
                    .bloodType(BloodType.A_POS)
                    .createdAt(now)
                    .version(1L)
                    .deleted(true)
                    .build();

            // Then
            assertThat(patient1).isEqualTo(patient2);
            assertThat(patient2).isEqualTo(patient1);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("All getters and setters work correctly")
        void gettersAndSetters_workCorrectly() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now();
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .createdAt(createdAt)
                    .build();

            // When
            patient.setId(1L);
            patient.setFirstName("Jane");
            patient.setLastName("Smith");
            patient.setDateOfBirth(LocalDate.of(1995, 5, 5));
            patient.setGender(Gender.FEMALE);
            patient.setInsuranceNumber("INS654321");
            patient.setBloodType(BloodType.A_POS);
            patient.setCreatedAt(createdAt.plusDays(1));
            patient.setVersion(2L);
            patient.setDeleted(true);

            // Then
            assertThat(patient.getId()).isEqualTo(1L);
            assertThat(patient.getFirstName()).isEqualTo("Jane");
            assertThat(patient.getLastName()).isEqualTo("Smith");
            assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 5));
            assertThat(patient.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(patient.getInsuranceNumber()).isEqualTo("INS654321");
            assertThat(patient.getBloodType()).isEqualTo(BloodType.A_POS);
            assertThat(patient.getCreatedAt()).isEqualTo(createdAt.plusDays(1));
            assertThat(patient.getVersion()).isEqualTo(2L);
            assertThat(patient.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Boolean getter for deleted field")
        void booleanGetter_forDeletedField() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .deleted(true)
                    .build();

            // Then
            assertThat(patient.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Enums Integration Tests")
    class EnumsIntegrationTests {

        @ParameterizedTest
        @MethodSource("provideAllGenders")
        @DisplayName("All gender enums can be set")
        void allGenderEnums_canBeSet(Gender gender) {
            // Given & When
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(gender)
                    .insuranceNumber("INS123456")
                    .bloodType(BloodType.O_POS)
                    .build();

            // Then
            assertThat(patient.getGender()).isEqualTo(gender);
        }

        @ParameterizedTest
        @MethodSource("provideAllBloodTypes")
        @DisplayName("All blood type enums can be set")
        void allBloodTypeEnums_canBeSet(BloodType bloodType) {
            // Given & When
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().minusYears(30))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS123456")
                    .bloodType(bloodType)
                    .build();

            // Then
            assertThat(patient.getBloodType()).isEqualTo(bloodType);
        }

        private static Stream<Arguments> provideAllGenders() {
            return Stream.of(
                    Arguments.of(Gender.MALE),
                    Arguments.of(Gender.FEMALE),
                    Arguments.of(Gender.OTHER)
            );
        }

        private static Stream<Arguments> provideAllBloodTypes() {
            return Stream.of(
                    Arguments.of(BloodType.O_POS),
                    Arguments.of(BloodType.O_NEG),
                    Arguments.of(BloodType.A_POS),
                    Arguments.of(BloodType.A_NEG),
                    Arguments.of(BloodType.B_POS),
                    Arguments.of(BloodType.B_NEG),
                    Arguments.of(BloodType.AB_POS),
                    Arguments.of(BloodType.AB_NEG)
            );
        }
    }
}