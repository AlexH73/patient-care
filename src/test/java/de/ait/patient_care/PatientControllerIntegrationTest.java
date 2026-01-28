package de.ait.patient_care;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.patient_care.entity.Patient;
import de.ait.patient_care.enums.BloodType;
import de.ait.patient_care.enums.Gender;
import de.ait.patient_care.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient validPatient;

    @BeforeEach
    void setup() {
        patientRepository.deleteAll();

        validPatient = new Patient();
        validPatient.setFirstName("Anna");
        validPatient.setLastName("Smith");
        validPatient.setDateOfBirth(LocalDate.of(1985, 5, 10));
        validPatient.setGender(Gender.FEMALE);
        validPatient.setBloodType(BloodType.O_POS);
        validPatient.setInsuranceNumber("123456");
    }

    @Test
    void createPatient_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void createPatient_invalid_shouldReturn400() throws Exception {
        validPatient.setFirstName(""); // invalid

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPatientById_shouldReturn200() throws Exception {
        Patient saved = patientRepository.save(validPatient);

        mockMvc.perform(get("/api/patients/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void getPatientById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePatient_shouldReturn204() throws Exception {
        Patient saved = patientRepository.save(validPatient);

        mockMvc.perform(delete("/api/patients/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePatient_shouldReturn200() throws Exception {
        Patient saved = patientRepository.save(validPatient);

        saved.setFirstName("UpdatedName");

        mockMvc.perform(put("/api/patients/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"));
    }
}


