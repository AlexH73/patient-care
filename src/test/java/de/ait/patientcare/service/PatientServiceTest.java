package de.ait.patientcare.service;

import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.entity.enums.BloodType;
import de.ait.patientcare.entity.enums.Gender;
import de.ait.patientcare.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 28.01.2026
 * Project : PatientCare
 * ----------------------------------------------------------------------------
 */

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private static final String DEFAULT_INSURANCE_NUMBER = "INS123456";
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.now();

    private Patient basePatient;

    @BeforeEach
    void setUp() {
        basePatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(DEFAULT_BIRTH_DATE)
                .gender(Gender.MALE)
                .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                .bloodType(BloodType.O_POS)
                .createdAt(DEFAULT_CREATED_AT)
                .deleted(false)
                .version(0L)
                .build();
    }

    private Patient createPatient(Long id, String firstName, String lastName,
                                  LocalDate dateOfBirth, Gender gender,
                                  String insuranceNumber, BloodType bloodType) {
        return Patient.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .insuranceNumber(insuranceNumber)
                .bloodType(bloodType)
                .createdAt(DEFAULT_CREATED_AT)
                .deleted(false)
                .version(0L)
                .build();
    }

    private Patient createPatientWithAge(Long id, String firstName, int age,
                                         Gender gender, BloodType bloodType) {
        LocalDate birthDate = LocalDate.now().minusYears(age);
        return createPatient(id, firstName, "Test", birthDate, gender,
                "INS" + id, bloodType);
    }

    @Nested
    @DisplayName("Create Patient Tests")
    class CreatePatientTests {

        @Test
        @DisplayName("Create patient - success")
        void createPatient_success() {
            Patient newPatient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(DEFAULT_BIRTH_DATE)
                    .gender(Gender.MALE)
                    .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                    .bloodType(BloodType.O_POS)
                    .build();

            when(patientRepository.existsByInsuranceNumber(DEFAULT_INSURANCE_NUMBER)).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(basePatient);

            Patient result = patientService.createPatient(newPatient);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getInsuranceNumber()).isEqualTo(DEFAULT_INSURANCE_NUMBER);

            verify(patientRepository).existsByInsuranceNumber(DEFAULT_INSURANCE_NUMBER);
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Create patient with duplicate insurance number - exception")
        void createPatient_duplicateInsuranceNumber_throwsException() {
            Patient newPatient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(DEFAULT_BIRTH_DATE)
                    .gender(Gender.MALE)
                    .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                    .bloodType(BloodType.O_POS)
                    .build();

            when(patientRepository.existsByInsuranceNumber(DEFAULT_INSURANCE_NUMBER)).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(newPatient))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessage("Insurance number must be unique");

            verify(patientRepository, never()).save(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Get Patient Tests")
    class GetPatientTests {

        @Test
        @DisplayName("Get patient by ID - success")
        void getPatientById_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(basePatient));

            Patient result = patientService.getPatientById(1L);

            assertThat(result).isEqualTo(basePatient);
            verify(patientRepository).findById(1L);
        }

        @Test
        @DisplayName("Get patient by non-existent ID - exception")
        void getPatientById_notFound_throwsException() {
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.getPatientById(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Patient not found");
        }

        @Test
        @DisplayName("Get deleted patient - exception")
        void getPatientById_deletedPatient_throwsException() {
            Patient deletedPatient = basePatient.toBuilder()
                    .deleted(true)
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(deletedPatient));

            assertThatThrownBy(() -> patientService.getPatientById(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Patient not found");
        }

        @Test
        @DisplayName("Get all active patients")
        void getAllPatients_returnsOnlyActive() {
            Patient patient1 = basePatient;
            Patient patient2 = basePatient.toBuilder()
                    .id(2L)
                    .firstName("Jane")
                    .insuranceNumber("INS654321")
                    .build();

            List<Patient> activePatients = Arrays.asList(patient1, patient2);

            when(patientRepository.findByDeletedFalse()).thenReturn(activePatients);

            List<Patient> result = patientService.getAllPatients();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

            verify(patientRepository).findByDeletedFalse();
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Update patient - success")
        void updatePatient_success() {
            Patient updatedData = Patient.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .gender(Gender.MALE)
                    .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                    .bloodType(BloodType.A_POS)
                    .build();

            Patient updatedPatient = basePatient.toBuilder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .bloodType(BloodType.A_POS)
                    .version(1L)
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(basePatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

            Patient result = patientService.updatePatient(1L, updatedData);

            assertThat(result.getFirstName()).isEqualTo("John Updated");
            assertThat(result.getLastName()).isEqualTo("Doe Updated");
            assertThat(result.getBloodType()).isEqualTo(BloodType.A_POS);
            assertThat(result.getVersion()).isEqualTo(1L);

            verify(patientRepository).findById(1L);
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Update non-existent patient - exception")
        void updatePatient_nonExistentId_throwsException() {
            Patient updatedData = Patient.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .gender(Gender.MALE)
                    .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                    .bloodType(BloodType.A_POS)
                    .build();

            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.updatePatient(999L, updatedData))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Patient not found");

            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Check insurance number uniqueness during update")
        void updatePatient_changeInsuranceNumber_checksUniqueness() {
            Patient updatedData = Patient.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .gender(Gender.MALE)
                    .insuranceNumber("INS_NEW_123")
                    .bloodType(BloodType.A_POS)
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(basePatient));
            when(patientRepository.existsByInsuranceNumber("INS_NEW_123")).thenReturn(true);

            assertThatThrownBy(() -> patientService.updatePatient(1L, updatedData))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessage("Insurance number must be unique");

            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Update patient without changing insurance number - success")
        void updatePatient_sameInsuranceNumber_success() {
            Patient updatedData = Patient.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .gender(Gender.MALE)
                    .insuranceNumber(DEFAULT_INSURANCE_NUMBER)
                    .bloodType(BloodType.A_POS)
                    .build();

            Patient updatedPatient = basePatient.toBuilder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dateOfBirth(LocalDate.of(1991, 2, 2))
                    .bloodType(BloodType.A_POS)
                    .version(1L)
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(basePatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

            Patient result = patientService.updatePatient(1L, updatedData);

            assertThat(result.getFirstName()).isEqualTo("John Updated");
            assertThat(result.getInsuranceNumber()).isEqualTo(DEFAULT_INSURANCE_NUMBER);

            verify(patientRepository, never()).existsByInsuranceNumber(anyString());
            verify(patientRepository).save(any(Patient.class));
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Soft delete patient")
        void deletePatient_softDelete() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(basePatient));
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
                Patient patient = invocation.getArgument(0);
                return patient.toBuilder()
                        .deleted(true)
                        .build();
            });

            patientService.deletePatient(1L);

            verify(patientRepository).findById(1L);
            verify(patientRepository).save(argThat(patient -> patient.isDeleted()));
        }
    }

    @Nested
    @DisplayName("Search Patients Tests")
    class SearchPatientsTests {

        @Test
        @DisplayName("Search patients with all filters - correct logic")
        void searchPatients_withFilters_correctLogic() {
            LocalDate today = LocalDate.now();
            LocalDate birthBefore = today.minusYears(20);
            LocalDate birthAfter = today.minusYears(40);

            Patient patientInRange = createPatientWithAge(2L, "Jane", 30,
                    Gender.FEMALE, BloodType.A_POS);

            when(patientRepository.search(
                    eq(Gender.FEMALE),
                    eq(BloodType.A_POS),
                    eq(birthBefore),
                    eq(birthAfter)
            )).thenReturn(List.of(patientInRange));

            List<Patient> result = patientService.searchPatients(
                    Gender.FEMALE,
                    BloodType.A_POS,
                    20,
                    40
            );

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(2L);
            assertThat(result.get(0).getGender()).isEqualTo(Gender.FEMALE);
            assertThat(result.get(0).getBloodType()).isEqualTo(BloodType.A_POS);
        }

        @Test
        @DisplayName("Search without filters - returns all active patients")
        void searchPatients_withoutFilters_returnsAllActive() {
            Patient patient1 = createPatientWithAge(1L, "John", 25, Gender.MALE, BloodType.O_POS);
            Patient patient2 = createPatientWithAge(2L, "Jane", 30, Gender.FEMALE, BloodType.A_POS);
            Patient patient3 = createPatientWithAge(3L, "Bob", 35, Gender.MALE, BloodType.B_POS);

            List<Patient> allPatients = Arrays.asList(patient1, patient2, patient3);

            when(patientRepository.search(null, null, null, null))
                    .thenReturn(allPatients);

            List<Patient> result = patientService.searchPatients(null, null, null, null);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
            assertThat(result.get(1).getFirstName()).isEqualTo("Jane");
            assertThat(result.get(2).getFirstName()).isEqualTo("Bob");
        }

        @ParameterizedTest(name = "gender={0}, bloodType={1}, ageFrom={2}, ageTo={3}")
        @MethodSource("searchFilterProvider")
        @DisplayName("Search patients with various filters")
        void searchPatients_withVariousFilters(Gender gender, BloodType bloodType,
                                               Integer ageFrom, Integer ageTo) {
            LocalDate today = LocalDate.now();
            LocalDate birthBefore = ageFrom != null ? today.minusYears(ageFrom) : null;
            LocalDate birthAfter = ageTo != null ? today.minusYears(ageTo) : null;

            Patient testPatient = createPatientWithAge(1L, "Test", 30,
                    gender != null ? gender : Gender.MALE,
                    bloodType != null ? bloodType : BloodType.O_POS);

            when(patientRepository.search(
                    eq(gender), eq(bloodType), eq(birthBefore), eq(birthAfter)))
                    .thenReturn(List.of(testPatient));

            List<Patient> result = patientService.searchPatients(gender, bloodType, ageFrom, ageTo);

            assertThat(result).hasSize(1);

            if (gender != null) {
                assertThat(result.get(0).getGender()).isEqualTo(gender);
            }
            if (bloodType != null) {
                assertThat(result.get(0).getBloodType()).isEqualTo(bloodType);
            }
        }

        private static Stream<Arguments> searchFilterProvider() {
            return Stream.of(
                    Arguments.of(Gender.MALE, null, null, null),
                    Arguments.of(null, BloodType.O_POS, null, null),
                    Arguments.of(null, null, 18, null),
                    Arguments.of(null, null, null, 65),
                    Arguments.of(Gender.FEMALE, BloodType.A_POS, 20, 40)
            );
        }

        @Test
        @DisplayName("Search with empty result")
        void searchPatients_emptyResult() {
            LocalDate today = LocalDate.now();
            LocalDate birthBefore = today.minusYears(100);
            LocalDate birthAfter = today.minusYears(120);

            when(patientRepository.search(
                    eq(Gender.FEMALE), eq(BloodType.AB_NEG), eq(birthBefore), eq(birthAfter)))
                    .thenReturn(List.of());

            List<Patient> result = patientService.searchPatients(
                    Gender.FEMALE, BloodType.AB_NEG, 100, 120);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Search with ageFrom > ageTo - should return empty list")
        void searchPatients_ageFromGreaterThanAgeTo() {
            LocalDate today = LocalDate.now();
            LocalDate birthBefore = today.minusYears(40);
            LocalDate birthAfter = today.minusYears(20);

            when(patientRepository.search(
                    isNull(), isNull(), eq(birthBefore), eq(birthAfter)))
                    .thenReturn(List.of());

            List<Patient> result = patientService.searchPatients(null, null, 40, 20);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Get statistics")
        void getStatistics_returnsCorrectData() {
            when(patientRepository.countByDeletedFalse()).thenReturn(10L);
            when(patientRepository.countByGender(Gender.MALE)).thenReturn(5L);
            when(patientRepository.countByGender(Gender.FEMALE)).thenReturn(4L);
            when(patientRepository.countByGender(Gender.OTHER)).thenReturn(1L);
            when(patientRepository.countByDateOfBirthBefore(any(LocalDate.class))).thenReturn(3L);

            var result = patientService.getStatistics();

            assertThat(result).hasSize(5);
            assertThat(result.get("totalPatients")).isEqualTo(10L);
            assertThat(result.get("maleCount")).isEqualTo(5L);
            assertThat(result.get("femaleCount")).isEqualTo(4L);
            assertThat(result.get("otherCount")).isEqualTo(1L);
            assertThat(result.get("olderThan60")).isEqualTo(3L);

            verify(patientRepository).countByDeletedFalse();
            verify(patientRepository, times(3)).countByGender(any());
            verify(patientRepository).countByDateOfBirthBefore(any(LocalDate.class));
            verifyNoMoreInteractions(patientRepository);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, 'John', 25, MALE, O_POS",
            "2, 'Jane', 30, FEMALE, A_POS",
            "3, 'Bob', 35, MALE, B_POS",
            "4, 'Alice', 40, FEMALE, AB_POS"
    })
    @DisplayName("Create test patient with different parameters")
    void createTestPatient_withDifferentParameters(Long id, String firstName, int age,
                                                   Gender gender, BloodType bloodType) {
        Patient patient = createPatientWithAge(id, firstName, age, gender, bloodType);

        assertThat(patient.getId()).isEqualTo(id);
        assertThat(patient.getFirstName()).isEqualTo(firstName);
        assertThat(patient.getGender()).isEqualTo(gender);
        assertThat(patient.getBloodType()).isEqualTo(bloodType);
    }
}