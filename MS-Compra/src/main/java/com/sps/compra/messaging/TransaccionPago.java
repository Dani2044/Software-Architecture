package com.sps.compra.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Evento que llega a ColaPagoConfirmado publicado por SaludPay-Back (.NET).
 *
 * Tolera campos extra (como fechaPago en formato ISO con Z que LocalDateTime
 * de Java no parsea) usando {@code @JsonIgnoreProperties}. MS-Compra solo
 * necesita {@code numeroCompra} y {@code valorPagado} para procesar el pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransaccionPago {
    private String cedula;
    private Long numeroCompra;
    private BigDecimal valorPagado;
    // fechaPago se omite — MS-Compra registra su propia fecha al marcar PAGADA
}
