package com.sps.compra.messaging;

import com.sps.compra.service.CompraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PagoListener {

    private final CompraService compraService;

    @JmsListener(destination = "${sps.cola.pago:cola.pago}")
    public void onPago(PagoEvento evento) {
        log.info("[cola.pago] Pago recibido compra={} valor={}",
                evento.getNumeroCompra(), evento.getValorPagado());
        compraService.marcarComoPagada(evento.getNumeroCompra(), evento.getValorPagado());
    }
}
