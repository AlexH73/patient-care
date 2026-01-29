package de.ait.patientcare.unit.handler;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import de.ait.patientcare.handler.GlobalExceptionHandler;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Handle runtime exception - returns not found with error message")
    void handleRuntimeException_returnsNotFoundWithErrorMessage() {
        // Given
        String errorMessage = "Patient not found with id: 999";
        RuntimeException exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response =
                exceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", errorMessage);
    }

    @Test
    @DisplayName("Handle optimistic lock exception - returns conflict with specific message")
    void handleOptimisticLockException_returnsConflictWithSpecificMessage() {
        // Given
        OptimisticLockException exception = new OptimisticLockException();

        // When
        ResponseEntity<Map<String, String>> response =
                exceptionHandler.handleOptimisticLock(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("code", "OPTIMISTIC_LOCK");
        assertThat(response.getBody().get("error"))
                .contains("data has been changed");
    }

    @Test
    @DisplayName("Handle different exception types correctly")
    void handleDifferentExceptionTypes_correctly() {
        // Test 1: RuntimeException
        ResponseEntity<Map<String, String>> runtimeResponse =
                exceptionHandler.handleRuntimeException(new RuntimeException("Test"));
        assertThat(runtimeResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Test 2: OptimisticLockException
        ResponseEntity<Map<String, String>> optimisticResponse =
                exceptionHandler.handleOptimisticLock(new OptimisticLockException());
        assertThat(optimisticResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}