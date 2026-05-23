package com.sps.sam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa el evento de compra finalizada recibido a traves
 * de la cola JMS {@code cola.sam}.
 *
 * <p>Este objeto es deserializado automaticamente por el convertidor Jackson
 * configurado en {@link com.sps.sam.config.JmsConfig}. Contiene la informacion
 * minima necesaria para que SAM registre los servicios medicos adquiridos
 * en la agenda del paciente.</p>
 *
 * <p>Estructura del mensaje:</p>
 * <ul>
 *   <li>{@code numeroCompra} - identificador unico de la compra en el SPS</li>
 *   <li>{@code cedulaCliente} - cedula del paciente que realizo la compra</li>
 *   <li>{@code servicios} - lista de servicios medicos incluidos en la compra</li>
 * </ul>
 *
 * @author SPS Team
 * @see com.sps.sam.listener.SamListener
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraTerminadaSamDto {

    /** Numero unico de la compra asignado por el modulo MS-Compra. */
    private Long numeroCompra;

    /** Cedula de identidad del cliente/paciente que realizo la compra. */
    private String cedulaCliente;

    /** Lista de servicios medicos adquiridos dentro de esta compra. */
    private List<ServicioMedicoDto> servicios;

    /**
     * DTO anidado que describe un servicio medico individual dentro de la compra.
     *
     * <p>Cada servicio medico tiene un codigo unico del catalogo, un nombre
     * descriptivo y una duracion estimada en minutos para la cita.</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioMedicoDto {

        /** Codigo unico del servicio medico en el catalogo del SPS. */
        private String codigo;

        /** Nombre descriptivo del servicio medico (ej. "Consulta General"). */
        private String nombre;

        /** Duracion estimada del servicio en minutos. */
        private Integer duracionMinutos;
    }
}
