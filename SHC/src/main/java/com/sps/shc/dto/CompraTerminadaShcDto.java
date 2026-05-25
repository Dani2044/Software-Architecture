package com.sps.shc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO (Data Transfer Object) que representa el evento de compra terminada
 * recibido desde la cola JMS {@code ColaSHC}.
 *
 * <p>Este objeto es deserializado automáticamente por el convertidor Jackson
 * configurado en {@link com.sps.shc.config.JmsConfig}. Contiene la información
 * necesaria para crear uno o más registros de historia clínica:</p>
 * <ul>
 *   <li>El número de compra que identifica la transacción de origen.</li>
 *   <li>Los datos de la persona (paciente/comprador).</li>
 *   <li>La lista de planes de salud adquiridos en la compra.</li>
 * </ul>
 *
 * <p>Por cada combinación de {@code numeroCompra} + {@code plan.codigo} se genera
 * un registro individual de {@link com.sps.shc.entity.PlanSalud}.</p>
 *
 * @see com.sps.shc.listener.ListenerSHC
 * @see com.sps.shc.service.SrvSHC#registrarCompra(CompraTerminadaShcDto)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraTerminadaShcDto {

    /** Identificador único de la compra originada en MS-Compra. */
    private Long numeroCompra;

    /** Datos de la persona asociada a la compra (paciente/comprador). */
    private PersonaDto persona;

    /** Lista de planes de salud incluidos en la compra. */
    private List<PlanDto> planes;

    /**
     * DTO interno que representa los datos básicos de una persona (paciente/comprador).
     *
     * <p>Contiene la información de identificación y contacto necesaria
     * para registrar la historia clínica.</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonaDto {
        /** Número de cédula (documento de identidad) de la persona. */
        private String cedula;
        /** Nombre completo de la persona. */
        private String nombre;
        /** Dirección de correo electrónico de la persona. */
        private String correo;
    }

    /**
     * DTO interno que representa un plan de salud adquirido en la compra.
     *
     * <p>Cada plan genera un registro independiente en la tabla de historia clínica,
     * permitiendo el seguimiento individual por plan.</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanDto {
        /** Código único que identifica el plan de salud. */
        private String codigo;
        /** Nombre descriptivo del plan de salud. */
        private String nombre;
        /** Precio del plan de salud al momento de la compra. */
        private Double precio;
    }
}
