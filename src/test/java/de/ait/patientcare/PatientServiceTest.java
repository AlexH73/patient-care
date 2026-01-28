package de.ait.patientcare;

import de.ait.patientcare.handler.NotFoundException;
import de.ait.patientcare.service.PatientService;
import de.ait.patientcare.entity.Patient;
import de.ait.patientcare.enums.BloodType;
import de.ait.patientcare.enums.Gender;
import de.ait.patientcare.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository repo;

    @InjectMocks
    private PatientService service;

    @Test
    void getAll_shouldReturnPatients() {
        when(repo.findByDeletedFalse()).thenReturn(List.of(new Patient()));

        List<Patient> result = service.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_shouldReturnPatient() {
        Patient p = new Patient();
        p.setDeleted(false);

        when(repo.findById(1L)).thenReturn(Optional.of(p));

        Patient result = service.getById(1L);

        assertThat(result).isEqualTo(p);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_shouldSavePatient() {
        Patient p = new Patient();
        when(repo.save(p)).thenReturn(p);

        Patient result = service.create(p);

        assertThat(result).isEqualTo(p);
    }

    @Test
    void update_shouldModifyFields() {
        Patient existing = new Patient();
        existing.setId(1L);

        Patient updated = new Patient();
        updated.setFirstName("New");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        Patient result = service.update(1L, updated);

        assertThat(result.getFirstName()).isEqualTo("New");
    }

    @Test
    void softDelete_shouldMarkAsDeleted() {
        Patient p = new Patient();
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        service.softDelete(1L);

        assertThat(p.isDeleted()).isTrue();
    }

    @Test
    void search_shouldCallRepository() {
        when(repo.search(any(), any(), any(), any())).thenReturn(List.of());

        List<Patient> result = service.search(Gender.MALE, BloodType.O_POS, 20, 40);

        assertThat(result).isEmpty();
    }

    @Test
    void statistics_shouldReturnMap() {
        when(repo.countByDeletedFalse()).thenReturn(10L);
        when(repo.countByGender(Gender.MALE)).thenReturn(5L);
        when(repo.countByGender(Gender.FEMALE)).thenReturn(4L);
        when(repo.countByGender(Gender.OTHER)).thenReturn(1L);
        when(repo.countOlderThan(any())).thenReturn(3L);

        Map<String, Object> stats = service.statistics();

        assertThat(stats).containsKeys(
                "totalPatients", "maleCount", "femaleCount", "otherCount", "olderThan60"
        );
    }
}
