package co.sps.sns.model;

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
 * <p>Persistida en la tabla {@code solicitudes_afiliacion} de la base de datos H2.</p>
 *
 * @author SPS Team
 * @version 1.0
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
        // Se registra la fecha/hora actual como momento de creacion
        fechaCreacion = LocalDateTime.now();
        // Si no se especifico un estado, se asigna PENDIENTE por defecto
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }

    /**
     * Enumeracion que define los posibles estados de una solicitud de afiliacion.
     *
     * <ul>
     *   <li><b>PENDIENTE:</b> Solicitud recibida, aun sin procesar (equivale a ENPROCESO para MS-Compra)</li>
     *   <li><b>APROBADA:</b> Afiliacion validada y activa (equivale a APROBADO para MS-Compra)</li>
     *   <li><b>RECHAZADA:</b> Afiliacion rechazada por el sistema (equivale a RECHAZADO para MS-Compra)</li>
     *   <li><b>EN_REVISION:</b> Solicitud en revision manual por un funcionario</li>
     * </ul>
     */
    public enum EstadoSolicitud {
        PENDIENTE, APROBADA, RECHAZADA, EN_REVISION
    }

    /** @return el identificador unico de la solicitud */
    public Long getId() { return id; }

    /** @return el numero de documento del afiliado */
    public String getNumeroDocumento() { return numeroDocumento; }

    /** @param numeroDocumento numero de documento a asignar */
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    /** @return el tipo de documento (CC, TI, CE) */
    public String getTipoDocumento() { return tipoDocumento; }

    /** @param tipoDocumento tipo de documento a asignar */
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    /** @return el nombre completo del afiliado */
    public String getNombreAfiliado() { return nombreAfiliado; }

    /** @param nombreAfiliado nombre del afiliado a asignar */
    public void setNombreAfiliado(String nombreAfiliado) { this.nombreAfiliado = nombreAfiliado; }

    /** @return el nombre de la EPS */
    public String getEps() { return eps; }

    /** @param eps nombre de la EPS a asignar */
    public void setEps(String eps) { this.eps = eps; }

    /** @return el estado actual de la solicitud */
    public EstadoSolicitud getEstado() { return estado; }

    /** @param estado nuevo estado de la solicitud */
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    /** @return la fecha y hora de creacion de la solicitud */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /** @return la fecha y hora de la respuesta, o {@code null} si aun no se ha respondido */
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }

    /** @param fechaRespuesta fecha y hora de la respuesta a asignar */
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }

    /** @return las observaciones asociadas a la solicitud */
    public String getObservaciones() { return observaciones; }

    /** @param observaciones observaciones o comentarios a asignar */
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
