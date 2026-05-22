package co.sps.balanceador.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "balanceador_logs")
public class BalanceadorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipo;           // REQUEST | HEALTH_UP | HEALTH_DOWN | ERROR

    @Column(nullable = false, length = 10)
    private String metodo;         // GET | POST | HEALTH

    @Column(nullable = false)
    private String backendUrl;

    @Column(nullable = false)
    private String path;

    @Column(length = 500)
    private String detalle;        // mensaje de error o info adicional

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructor vacío requerido por JPA
    public BalanceadorLog() {}

    public BalanceadorLog(String tipo, String metodo, String backendUrl, String path, String detalle) {
        this.tipo = tipo;
        this.metodo = metodo;
        this.backendUrl = backendUrl;
        this.path = path;
        this.detalle = detalle;
        this.timestamp = LocalDateTime.now();
    }

    // Getters y setters
    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getMetodo() { return metodo; }
    public String getBackendUrl() { return backendUrl; }
    public String getPath() { return path; }
    public String getDetalle() { return detalle; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
