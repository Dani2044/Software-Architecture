package co.sps.sns.model;

/**
 * DTO (Data Transfer Object) que representa el resultado de una validacion
 * de afiliacion al sistema de salud.
 *
 * <p>Este objeto es la respuesta que el servicio SNS retorna cuando MS-Compra
 * realiza una consulta de validacion via WebClient al endpoint
 * {@code GET /api/sns/validar}. Contiene toda la informacion necesaria para
 * que MS-Compra determine si puede proceder con la compra del plan de salud.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Es el contrato de respuesta entre el
 * simulador SNS y MS-Compra. El campo {@code afiliado} indica si el usuario
 * tiene una afiliacion activa (equivalente a APROBADO), mientras que el campo
 * {@code mensaje} proporciona el detalle del resultado (APROBADO, RECHAZADO
 * o ENPROCESO).</p>
 *
 * @author SPS Team
 * @version 1.0
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

    /**
     * Constructor por defecto requerido para la serializacion/deserializacion JSON.
     */
    public ValidacionAfiliado() {}

    /**
     * Constructor completo para crear una respuesta de validacion con todos los campos.
     *
     * @param afiliado        {@code true} si el usuario tiene afiliacion activa
     * @param numeroDocumento numero de documento consultado
     * @param nombreAfiliado  nombre del afiliado (puede ser {@code null} si no existe)
     * @param eps             nombre de la EPS (puede ser {@code null} si no existe)
     * @param regimen         regimen de salud (puede ser {@code null} si no existe)
     * @param mensaje         mensaje descriptivo del resultado
     */
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

    /** @return {@code true} si el usuario tiene afiliacion activa */
    public boolean isAfiliado() { return afiliado; }

    /** @param afiliado estado de afiliacion a asignar */
    public void setAfiliado(boolean afiliado) { this.afiliado = afiliado; }

    /** @return el numero de documento consultado */
    public String getNumeroDocumento() { return numeroDocumento; }

    /** @param numeroDocumento numero de documento a asignar */
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    /** @return el nombre del afiliado */
    public String getNombreAfiliado() { return nombreAfiliado; }

    /** @param nombreAfiliado nombre del afiliado a asignar */
    public void setNombreAfiliado(String nombreAfiliado) { this.nombreAfiliado = nombreAfiliado; }

    /** @return el nombre de la EPS */
    public String getEps() { return eps; }

    /** @param eps nombre de la EPS a asignar */
    public void setEps(String eps) { this.eps = eps; }

    /** @return el regimen de salud (CONTRIBUTIVO o SUBSIDIADO) */
    public String getRegimen() { return regimen; }

    /** @param regimen regimen de salud a asignar */
    public void setRegimen(String regimen) { this.regimen = regimen; }

    /** @return el mensaje descriptivo del resultado de la validacion */
    public String getMensaje() { return mensaje; }

    /** @param mensaje mensaje a asignar */
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
