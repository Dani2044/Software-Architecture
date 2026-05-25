package com.sps.shc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un plan de salud adquirido por un paciente
 * dentro de una compra especifica.
 *
 * <p>Cada registro vincula un plan de salud del catalogo SPS con una compra
 * y un paciente (identificado por su cedula). La tabla subyacente es
 * {@code planes_salud} y posee una restriccion de unicidad compuesta
 * sobre ({@code numero_compra}, {@code codigo}) para garantizar la
 * idempotencia: un mismo plan dentro de una misma compra solo se
 * registra una vez.</p>
 *
 * <p>La fecha de registro se asigna automaticamente mediante el callback
 * {@link PrePersist} si no ha sido establecida previamente.</p>
 *
 * @author SPS Team
 * @see com.sps.shc.repository.RepoSHC
 * @see com.sps.shc.service.SrvSHC
 */
@Entity
@Table(name = "planes_salud",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero_compra", "codigo"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSalud {

    /** Identificador autoincremental del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de la compra de origen (referencia al MS-Compra). */
    @Column(name = "numero_compra", nullable = false)
    private Long numeroCompra;

    /** Cedula del paciente al que pertenece este plan adquirido. */
    @Column(name = "cedula_paciente", nullable = false, length = 32)
    private String cedulaPaciente;

    /** Codigo unico que identifica el plan de salud en el catalogo SPS. */
    @Column(name = "codigo", nullable = false, length = 64)
    private String codigo;

    /** Nombre descriptivo del plan de salud. */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Precio del plan de salud al momento de la compra. */
    @Column(name = "precio")
    private Double precio;

    /** Fecha y hora en que se registro este plan en la historia clinica. */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Callback de JPA que se ejecuta antes de persistir la entidad.
     * Asigna la fecha de registro con la fecha/hora actual si no fue
     * establecida previamente.
     */
    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
