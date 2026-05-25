package com.sps.shc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un paciente/usuario registrado en el sistema SHC.
 *
 * <p>Almacena los datos basicos del paciente necesarios para asociar
 * historias clinicas (planes de salud adquiridos) a una persona
 * identificada por su cedula. La cedula es unica y sirve como
 * identificador de negocio.</p>
 *
 * <p>Los registros de {@code Usuario} se crean automaticamente la primera
 * vez que llega un evento de compra terminada con una cedula no registrada
 * previamente (patron find-or-create en {@link com.sps.shc.service.SrvSHC}).</p>
 *
 * @author SPS Team
 * @see com.sps.shc.entity.PlanSalud
 * @see com.sps.shc.service.SrvSHC
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

    /** Correo electronico del usuario. */
    @Column(name = "correo", length = 200)
    private String correo;
}
