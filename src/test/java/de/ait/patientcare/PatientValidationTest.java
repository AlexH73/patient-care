package de.ait.patientcare;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PatientValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidation_whenFirstNameIsBlank() {
        Patient patient = new Patient();
        patient.setFirstName(""); // invalid
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patient.setBloodType(BloodType.A_POS);
        patient.setInsuranceNumber("123456");

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldPassValidation_whenAllFieldsValid() {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patient.setBloodType(BloodType.A_POS);
        patient.setInsuranceNumber("123456");

        Set<ConstraintViolation<Patient>> violations = validator.validate(patient);

        assertThat(violations).isEmpty();
    }
}

