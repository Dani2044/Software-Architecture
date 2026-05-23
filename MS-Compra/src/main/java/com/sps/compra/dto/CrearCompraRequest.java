package com.sps.compra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CrearCompraRequest {

    @NotBlank
    private String cedulaCliente;

    @NotBlank
    private String nombreCliente;

    @Email
    @NotBlank
    private String correoCliente;

    @NotEmpty
    private List<PlanRequest> planes;

    @Data
    public static class PlanRequest {
        @NotBlank private String codigo;
        @NotBlank private String nombre;
        private BigDecimal precio;
        private List<ServicioRequest> servicios;
    }

    @Data
    public static class ServicioRequest {
        @NotBlank private String codigo;
        @NotBlank private String nombre;
        private Integer duracionMinutos;
    }
}
