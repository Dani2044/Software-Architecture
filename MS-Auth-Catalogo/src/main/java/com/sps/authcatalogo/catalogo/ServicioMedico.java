package com.sps.authcatalogo.catalogo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa un servicio medico disponible en el catalogo SPS.
 *
 * <p>Cada servicio medico tiene un tipo categorizado ({@link TipoServicio}),
 * un precio individual y una duracion estimada en minutos. Los servicios
 * se asocian a uno o mas {@link PlanSalud} mediante una relacion ManyToMany.</p>
 *
 * @see Plan
 * @see CatalogoController#servicios()
 */
@Entity
@Table(name = "servicio_medico")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioMedico {

    /** Identificador unico auto-generado del servicio. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico del servicio (ej. {@code SVC-CONS-001}). Identificador de negocio. */
    @Column(nullable = false, unique = true, length = 64)
    private String codigo;

    /** Nombre descriptivo del servicio (ej. "Consulta medicina general"). */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Categoria del servicio medico (consulta, examen, hospitalizacion o procedimiento). */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private TipoServicio tipo;

    /** Precio unitario del servicio en pesos colombianos. */
    @Column(precision = 14, scale = 2)
    private BigDecimal precio;

    /** Duracion estimada del servicio en minutos. */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    /**
     * Enumeracion que clasifica los tipos de servicio medico disponibles.
     *
     * <ul>
     *   <li>{@code CONSULTA} - Consulta medica general o especializada.</li>
     *   <li>{@code EXAMEN} - Examen de laboratorio o diagnostico.</li>
     *   <li>{@code HOSPITALIZACION} - Internacion hospitalaria.</li>
     *   <li>{@code PROCEDIMIENTO} - Procedimiento medico o quirurgico.</li>
     * </ul>
     */
    public enum TipoServicio {
        CONSULTA, EXAMEN, HOSPITALIZACION, PROCEDIMIENTO
    }
}
