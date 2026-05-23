package com.sps.compra.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento que llega a ColaPagoConfirmado publicado por SaludPay-Back.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionPago {
    private String cedula;
    private Long numeroCompra;
    private BigDecimal valorPagado;
    private LocalDateTime fechaPago;
}
