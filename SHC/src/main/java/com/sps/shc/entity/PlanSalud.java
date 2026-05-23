package com.sps.shc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un plan de salud en el sistema SHC.
 *
 * <p>Almacena la informacion de los planes de salud disponibles.
 * El codigo es unico y se utiliza como identificador de negocio.</p>
 */
@Entity
@Table(name = "planes_salud",
       uniqueConstraints = @UniqueConstraint(columnNames = {"codigo"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSalud {

    /** Identificador autoincremental del registro. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico que identifica el plan de salud. */
    @Column(name = "codigo", nullable = false, unique = true, length = 64)
    private String codigo;

    /** Nombre descriptivo del plan de salud. */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Precio del plan de salud. */
    @Column(name = "precio")
    private Double precio;
}
