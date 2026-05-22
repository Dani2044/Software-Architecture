package co.sps.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NotificacionRequest {

    @NotBlank
    @Email
    private String correoCliente;

    @NotBlank
    private String nombreCliente;

    @NotBlank
    private String numeroCompra;

    @NotNull
    @Positive
    private Double valorCompra;

    @NotNull
    private NotificacionEmail.TipoNotificacion tipo;

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getNumeroCompra() { return numeroCompra; }
    public void setNumeroCompra(String numeroCompra) { this.numeroCompra = numeroCompra; }
    public Double getValorCompra() { return valorCompra; }
    public void setValorCompra(Double valorCompra) { this.valorCompra = valorCompra; }
    public NotificacionEmail.TipoNotificacion getTipo() { return tipo; }
    public void setTipo(NotificacionEmail.TipoNotificacion tipo) { this.tipo = tipo; }
}
