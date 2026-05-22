package co.sps.sns.model;

public class ValidacionAfiliado {

    private boolean afiliado;
    private String numeroDocumento;
    private String nombreAfiliado;
    private String eps;
    private String regimen;
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
