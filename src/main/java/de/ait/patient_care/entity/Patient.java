package de.ait.patient_care.entity;

import de.ait.patient_care.enums.BloodType;
import de.ait.patient_care.enums.Gender;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @Past
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(nullable = false)
    private String insuranceNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
