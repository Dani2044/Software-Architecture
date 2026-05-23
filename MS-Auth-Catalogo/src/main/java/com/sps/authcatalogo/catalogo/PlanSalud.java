package com.sps.authcatalogo.catalogo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un plan de salud dentro del catalogo del sistema SPS.
 *
 * <p>Cada plan agrupa uno o mas {@link ServicioMedico} mediante una relacion
 * {@link ManyToMany}, modelada a traves de la tabla intermedia {@code plan_servicio}.
 * Los planes son consultados por el frontend para que el usuario seleccione
 * la cobertura deseada al momento de realizar una compra.</p>
 *
 * <p>La carga de servicios asociados es {@code EAGER} para garantizar que la
 * respuesta del endpoint {@code GET /api/catalogo/planes} incluya la lista
 * completa de servicios de cada plan en una sola consulta.</p>
 *
 * @see ServicioMedico
 * @see CatalogoController#planes()
 */
@Entity
@Table(name = "plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    /** Identificador unico auto-generado del plan. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo unico del plan (ej. {@code PLAN-BASICO-001}). Sirve como identificador de negocio. */
    @Column(nullable = false, unique = true, length = 64)
    private String codigo;

    /** Nombre descriptivo del plan (ej. "Plan Basico"). */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Precio total del plan en pesos colombianos. */
    @Column(precision = 14, scale = 2)
    private BigDecimal precio;

    /** Descripcion breve del plan y los servicios que incluye. */
    @Column(length = 500)
    private String descripcion;

    /**
     * Lista de servicios medicos incluidos en este plan.
     *
     * <p>Relacion ManyToMany mapeada en la tabla intermedia {@code plan_servicio}.
     * Se carga de forma EAGER para que las consultas del catalogo retornen
     * los servicios asociados sin necesidad de consultas adicionales.</p>
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "plan_servicio",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    @Builder.Default
    private List<ServicioMedico> servicios = new ArrayList<>();
}
