package co.sps.sns.dto;

/**
 * DTO (Data Transfer Object) que representa el resultado de una validacion
 * de afiliacion al sistema de salud.
 *
 * <p>Este objeto es la respuesta que el servicio SNS retorna cuando MS-Compra
 * realiza una consulta de validacion via WebClient al endpoint
 * {@code GET /api/sns/validar}. Contiene toda la informacion necesaria para
 * que MS-Compra determine si puede proceder con la compra del plan de salud.</p>
 *
 * @author SPS Team
 */
public class ValidacionAfiliado {

    /** Indica si el usuario tiene una afiliacion activa en el sistema de salud */
    private boolean afiliado;

    /** Numero de documento consultado */
    private String numeroDocumento;

    /** Nombre completo del afiliado (null si no se encontro) */
    private String nombreAfiliado;

    /** Nombre de la EPS del afiliado (null si no se encontro) */
    private String eps;

    /** Regimen de salud del afiliado: CONTRIBUTIVO o SUBSIDIADO (null si no se encontro) */
    private String regimen;

    /** Mensaje descriptivo del resultado de la validacion */
    private String mensaje;

    public ValidacionAfiliado() {}

    public ValidacionAfiliado(boolean afiliado, String numeroDocumento,
                               String nombreAfiliado, String eps,
                               String regimen, String mensaje) {
        this.afiliado = afiliado;
        this.numeroDocumento = numeroDocumento;
        this.nombreAfiliado = nombreAfiliado;
        this.eps = eps;
        this.regimen = regimen;
        this.mensaje = mensaje;
    }

    public boolean isAfiliado() { return afiliado; }
    public void setAfiliado(boolean afiliado) { this.afiliado = afiliado; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombreAfiliado() { return nombreAfiliado; }
    public void setNombreAfiliado(String nombreAfiliado) { this.nombreAfiliado = nombreAfiliado; }
    public String getEps() { return eps; }
    public void setEps(String eps) { this.eps = eps; }
    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
