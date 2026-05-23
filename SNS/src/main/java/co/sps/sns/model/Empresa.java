package co.sps.sns.model;

import jakarta.persistence.*;

/**
 * Entidad que representa una empresa aseguradora (EPS) registrada ante la SNS.
 *
 * <p>Modela las EPS que estan autorizadas por la Superintendencia Nacional
 * de Salud para ofrecer planes de salud a los afiliados.</p>
 */
@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String codigoAseguradora;

    public Empresa() {}

    public Empresa(String nit, String nombre, String codigoAseguradora) {
        this.nit = nit;
        this.nombre = nombre;
        this.codigoAseguradora = codigoAseguradora;
    }

    public Long getId() { return id; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoAseguradora() { return codigoAseguradora; }
    public void setCodigoAseguradora(String codigoAseguradora) { this.codigoAseguradora = codigoAseguradora; }
}
