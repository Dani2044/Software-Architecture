package com.sps.sam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un doctor registrado en el sistema SAM.
 *
 * <p>Cada registro almacena la informacion basica de un doctor, incluyendo
 * su nombre, especialidad medica y el codigo del servicio que ofrece.
 * La tabla subyacente es {@code doctores}.</p>
 *
 * @author SPS Team
 */
@Entity
@Table(name = "doctores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctores {

    /** Identificador auto-generado (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del doctor. */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Especialidad medica del doctor (ej. "Oftalmologia", "Cardiologia"). */
    @Column(name = "especialidad", nullable = false, length = 200)
    private String especialidad;

    /** Codigo del servicio medico que el doctor ofrece, referenciando el catalogo del SPS. */
    @Column(name = "codigo_servicio", nullable = false, length = 64)
    private String codigoServicio;
}
