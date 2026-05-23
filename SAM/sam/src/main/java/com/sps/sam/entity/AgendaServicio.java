package com.sps.sam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un servicio medico agendado para un paciente.
 *
 * <p>Cada registro vincula un servicio medico del catalogo con una compra
 * realizada por un paciente identificado por su cedula. La tabla subyacente
 * es {@code agenda_servicio}.</p>
 *
 * <p><b>Restriccion de unicidad:</b> la combinacion
 * ({@code numero_compra}, {@code codigo_servicio}) es unica, lo que garantiza
 * que un mismo servicio no se registre dos veces para la misma compra.
 * Esta restriccion a nivel de base de datos complementa la verificacion
 * de idempotencia que realiza {@link com.sps.sam.service.SrvSAM}
 * a nivel de aplicacion.</p>
 *
 * @author SPS Team
 * @see com.sps.sam.repository.RepoSAM
 * @see com.sps.sam.service.SrvSAM
 */
@Entity
@Table(name = "agenda_servicio",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero_compra", "codigo_servicio"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaServicio {

    /** Identificador auto-generado (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de la compra originada en MS-Compra. */
    @Column(name = "numero_compra", nullable = false)
    private Long numeroCompra;

    /** Cedula de identidad del paciente/cliente. */
    @Column(name = "cedula_cliente", nullable = false, length = 32)
    private String cedulaCliente;

    /** Codigo unico del servicio medico segun el catalogo del SPS. */
    @Column(name = "codigo_servicio", nullable = false, length = 64)
    private String codigoServicio;

    /** Nombre legible del servicio medico (ej. "Consulta Oftalmologica"). */
    @Column(name = "nombre_servicio", nullable = false, length = 200)
    private String nombreServicio;

    /** Duracion estimada del servicio en minutos; puede ser {@code null} si no se especifica. */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    /** Fecha y hora en que se registro este servicio en la agenda. */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Callback JPA que se ejecuta antes de persistir la entidad por primera vez.
     *
     * <p>Asigna automaticamente la fecha de registro con la hora actual
     * del servidor si no fue establecida previamente por el codigo de negocio.</p>
     */
    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
