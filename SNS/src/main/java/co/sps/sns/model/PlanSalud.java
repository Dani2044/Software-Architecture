package co.sps.sns.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un plan de salud registrado ante la SNS.
 *
 * <p>Modela los planes de salud que las aseguradoras (EPS) ofrecen
 * y que la Superintendencia Nacional de Salud tiene catalogados
 * para efectos de validacion.</p>
 */
@Entity
@Table(name = "planes_salud")
public class PlanSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String codigoAseguradora;

    public PlanSalud() {}

    public PlanSalud(String codigo, String nombre, String codigoAseguradora) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.codigoAseguradora = codigoAseguradora;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoAseguradora() { return codigoAseguradora; }
    public void setCodigoAseguradora(String codigoAseguradora) { this.codigoAseguradora = codigoAseguradora; }
}
