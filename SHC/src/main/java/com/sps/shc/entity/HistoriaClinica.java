package com.sps.shc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un registro de historia clínica en la base de datos.
 *
 * <p>Cada registro corresponde a la asociación entre una compra, una persona y un
 * plan de salud específico. La tabla impone una restricción de unicidad compuesta
 * sobre {@code (numero_compra, codigo_plan)} para garantizar la idempotencia:
 * un mismo plan dentro de una misma compra solo se registra una vez.</p>
 *
 * <p>La fecha de registro se asigna automáticamente mediante el callback
 * {@link PrePersist} si no ha sido establecida previamente.</p>
 *
 * @see com.sps.shc.repository.RepoSHC
 * @see com.sps.shc.service.SrvSHC
 */
@Entity
@Table(name = "historia_clinica",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero_compra", "codigo_plan"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinica {

    /** Identificador autoincremental del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de la compra de origen (referencia al MS-Compra). */
    @Column(name = "numero_compra", nullable = false)
    private Long numeroCompra;

    /** Cédula (documento de identidad) del paciente/comprador. */
    @Column(name = "cedula", nullable = false, length = 32)
    private String cedula;

    /** Nombre completo del paciente/comprador. */
    @Column(name = "nombre", length = 200)
    private String nombre;

    /** Correo electrónico del paciente/comprador. */
    @Column(name = "correo", length = 200)
    private String correo;

    /** Código único del plan de salud adquirido. */
    @Column(name = "codigo_plan", nullable = false, length = 64)
    private String codigoPlan;

    /** Nombre descriptivo del plan de salud. */
    @Column(name = "nombre_plan", nullable = false, length = 200)
    private String nombrePlan;

    /** Precio del plan de salud al momento de la compra. */
    @Column(name = "precio_plan")
    private Double precioPlan;

    /** Fecha y hora en que se registró la historia clínica. */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Callback de JPA que se ejecuta antes de persistir la entidad.
     * Asigna la fecha de registro con la fecha/hora actual si no fue establecida previamente.
     */
    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
