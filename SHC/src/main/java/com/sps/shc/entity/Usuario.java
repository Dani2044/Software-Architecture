package com.sps.shc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad JPA que representa un paciente/usuario registrado en el sistema SHC.
 *
 * <p>Almacena los datos basicos del paciente necesarios para asociar
 * historias clinicas a una persona identificada por su cedula.</p>
 */
@Entity
@Table(name = "usuarios",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cedula"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /** Identificador autoincremental del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cedula (documento de identidad) del usuario. Valor unico. */
    @Column(name = "cedula", nullable = false, unique = true, length = 32)
    private String cedula;

    /** Nombre completo del usuario. */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Fecha de nacimiento del usuario. */
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}
