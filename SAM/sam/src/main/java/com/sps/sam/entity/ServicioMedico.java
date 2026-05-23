package com.sps.sam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un servicio medico disponible en el catalogo del SPS.
 *
 * <p>Cada registro describe un servicio medico con su codigo unico, nombre
 * descriptivo y duracion estimada en minutos. La tabla subyacente
 * es {@code servicio_medico}.</p>
 *
 * @author SPS Team
 */
@Entity
@Table(name = "servicio_medico")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioMedico {

    /** Identificador auto-generado (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico del servicio medico en el catalogo del SPS. */
    @Column(name = "codigo", nullable = false, unique = true, length = 64)
    private String codigo;

    /** Nombre descriptivo del servicio medico (ej. "Consulta Oftalmologica"). */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /** Duracion estimada del servicio en minutos. */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;
}
