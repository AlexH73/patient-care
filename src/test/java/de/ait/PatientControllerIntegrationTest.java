package de.ait;


import de.ait.patient_care.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatientControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient client(WebTestClient.Builder builder) {
        return builder.baseUrl("http://localhost:" + port).build();
    }

    @Autowired
    private WebTestClient.Builder webClientBuilder;

    @Test
    void createPatient_success_shouldReturn201() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("Anna");
        patient.setLastName("Smith");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 10));
        patient.setGender(Gender.FEMALE);
        patient.setBloodType(BloodType.O_POSITIVE);
        patient.setInsuranceNumber("999999");

        client(webClientBuilder)
                .post()
                .uri("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(patient))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Anna");
    }

    @Test
    void createPatient_fail_shouldReturn400() throws Exception {
        Patient invalid = new Patient();
        invalid.setFirstName(""); // invalid
        invalid.setLastName("Smith");
        invalid.setDateOfBirth(LocalDate.of(1985, 5, 10));
        invalid.setGender(Gender.FEMALE);
        invalid.setBloodType(BloodType.O_POSITIVE);
        invalid.setInsuranceNumber("999999");

        client(webClientBuilder)
                .post()
                .uri("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(invalid))
                .exchange()
                .expectStatus().isBadRequest();
    }
}

