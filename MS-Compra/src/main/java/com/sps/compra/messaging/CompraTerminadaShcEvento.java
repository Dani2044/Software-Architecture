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
public class CompraTerminadaShcEvento {
    private Long numeroCompra;
    private PersonaMsg persona;
    private List<PlanMsg> planes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonaMsg {
        private String cedula;
        private String nombre;
        private String correo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanMsg {
        private String codigo;
        private String nombre;
        private Double precio;
    }
}
