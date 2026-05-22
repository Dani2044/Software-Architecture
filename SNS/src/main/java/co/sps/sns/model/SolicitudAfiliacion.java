package co.sps.sns.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_afiliacion")
public class SolicitudAfiliacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String numeroDocumento;

    @NotBlank
    @Column(nullable = false)
    private String tipoDocumento;

    @NotBlank
    @Column(nullable = false)
    private String nombreAfiliado;

    @NotBlank
    @Column(nullable = false)
    private String eps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaRespuesta;

    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }

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
