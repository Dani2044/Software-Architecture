package com.sps.compra.messaging;

import com.sps.compra.service.SrvCompras;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerPagos {

    private final SrvCompras srvCompras;

    @JmsListener(destination = "${sps.cola.pago:ColaPagoConfirmado}")
    public void onPago(TransaccionPago evento) {
        log.info("[ColaPagoConfirmado] Pago recibido compra={} valor={}",
                evento.getNumeroCompra(), evento.getValorPagado());
        srvCompras.marcarComoPagada(evento.getNumeroCompra(), evento.getValorPagado());
    }
}
