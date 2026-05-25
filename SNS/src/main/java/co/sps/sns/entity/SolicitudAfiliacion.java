package co.sps.sns.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una solicitud de afiliacion al sistema de salud.
 *
 * <p>Modela la informacion de un afiliado registrado ante la Superintendencia
 * Nacional de Salud (SNS), incluyendo sus datos personales, la EPS a la que
 * pertenece y el estado de su solicitud de afiliacion.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Esta entidad es la estructura de datos
 * principal del simulador SNS. Cuando MS-Compra invoca el endpoint de
 * validacion via WebClient, el servicio consulta esta tabla para determinar
 * si el usuario tiene una afiliacion activa (APROBADA), rechazada (RECHAZADA)
 * o en proceso (PENDIENTE/EN_REVISION).</p>
 *
 * <p>Persistida en la tabla {@code solicitudes_afiliacion} de la base de datos.</p>
 *
 * @author SPS Team
 */
@Entity
@Table(name = "solicitudes_afiliacion")
public class SolicitudAfiliacion {

    /** Identificador unico autogenerado de la solicitud */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero de documento del afiliado (obligatorio, no puede estar en blanco) */
    @NotBlank
    @Column(nullable = false)
    private String numeroDocumento;

    /** Tipo de documento: CC (Cedula), TI (Tarjeta de Identidad), CE (Cedula de Extranjeria) */
    @NotBlank
    @Column(nullable = false)
    private String tipoDocumento;

    /** Nombre completo del afiliado */
    @NotBlank
    @Column(nullable = false)
    private String nombreAfiliado;

    /** Nombre de la EPS a la que pertenece el afiliado (ej: SURA, COMPENSAR, FAMISANAR) */
    @NotBlank
    @Column(nullable = false)
    private String eps;

    /** Estado actual de la solicitud de afiliacion */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    /** Fecha y hora de creacion de la solicitud (se asigna automaticamente, no actualizable) */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha y hora en que se emitio una respuesta sobre la solicitud */
    private LocalDateTime fechaRespuesta;

    /** Observaciones o comentarios sobre la decision tomada */
    private String observaciones;

    /**
     * Callback de JPA que se ejecuta antes de persistir la entidad por primera vez.
     *
     * <p>Asigna automaticamente la fecha de creacion y establece el estado
     * como PENDIENTE si no fue definido previamente.</p>
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }

    /**
     * Enumeracion que define los posibles estados de una solicitud de afiliacion.
     */
    public enum EstadoSolicitud {
        PENDIENTE, APROBADA, RECHAZADA, EN_REVISION
    }

    public Long getId() { return id; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNombreAfiliado() { return nombreAfiliado; }
    public void setNombreAfiliado(String nombreAfiliado) { this.nombreAfiliado = nombreAfiliado; }
    public String getEps() { return eps; }
    public void setEps(String eps) { this.eps = eps; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
