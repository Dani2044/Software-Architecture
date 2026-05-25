package com.sps.compra.messaging;

import com.sps.compra.service.SrvCompras;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener AMQP que consume mensajes de la cola {@code ColaPagoConfirmado}.
 *
 * <p>Cuando SaludPay-Back confirma un pago, publica un mensaje en esta cola.
 * El {@link com.sps.compra.config.RabbitMQConfiguration} configura el
 * {@code Jackson2JsonMessageConverter} con {@code DefaultClassMapper} para
 * deserializar automaticamente el JSON a {@link TransaccionPago}.</p>
 *
 * @see com.sps.compra.config.RabbitMQConfiguration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerPagos {

    private final SrvCompras srvCompras;

    @RabbitListener(queues = "${sps.cola.pago:ColaPagoConfirmado}")
    public void onPago(TransaccionPago evento) {
        log.info("[ColaPagoConfirmado] Pago recibido compra={} valor={}",
                evento.getNumeroCompra(), evento.getValorPagado());
        srvCompras.marcarComoPagada(evento.getNumeroCompra(), evento.getValorPagado());
    }
}
