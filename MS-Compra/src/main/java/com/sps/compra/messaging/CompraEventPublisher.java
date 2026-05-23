package com.sps.compra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraEventPublisher {

    private final JmsTemplate jmsTemplate;

    @Value("${sps.cola.sam:cola.sam}")
    private String colaSam;

    @Value("${sps.cola.shc:cola.shc}")
    private String colaShc;

    public void notificarSam(CompraTerminadaSamEvento evento) {
        log.info("Publicando en {} -> compra {}", colaSam, evento.getNumeroCompra());
        jmsTemplate.convertAndSend(colaSam, evento);
    }

    public void notificarShc(CompraTerminadaShcEvento evento) {
        log.info("Publicando en {} -> compra {}", colaShc, evento.getNumeroCompra());
        jmsTemplate.convertAndSend(colaShc, evento);
    }
}
