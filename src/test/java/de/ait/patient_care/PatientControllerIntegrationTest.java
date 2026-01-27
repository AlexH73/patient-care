package de.ait.patient_care;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.patient_care.entity.Patient;
import de.ait.patient_care.enums.BloodType;
import de.ait.patient_care.enums.Gender;
import de.ait.patient_care.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void createPatient_success_shouldReturn201() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("Anna");
        patient.setLastName("Smith");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 10));
        patient.setGender(Gender.FEMALE);
        patient.setBloodType(BloodType.O_POS);
        patient.setInsuranceNumber("999999");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void createPatient_fail_shouldReturn400() throws Exception {
        Patient invalid = new Patient();
        invalid.setFirstName(""); // invalid
        invalid.setLastName("Smith");
        invalid.setDateOfBirth(LocalDate.of(1985, 5, 10));
        invalid.setGender(Gender.FEMALE);
        invalid.setBloodType(BloodType.O_POS);
        invalid.setInsuranceNumber("999999");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}

