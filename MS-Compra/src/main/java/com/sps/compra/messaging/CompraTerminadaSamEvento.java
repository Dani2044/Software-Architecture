package com.sps.compra.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraTerminadaSamEvento {
    private Long numeroCompra;
    private String cedulaCliente;
    private List<ServicioMedicoMsg> servicios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioMedicoMsg {
        private String codigo;
        private String nombre;
        private Integer duracionMinutos;
    }
}
