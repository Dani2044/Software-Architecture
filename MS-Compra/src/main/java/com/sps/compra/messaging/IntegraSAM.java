package com.sps.compra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegraSAM {

    private final JmsTemplate jmsTemplate;

    @Value("${sps.cola.sam:ColaSAM}")
    private String colaSam;

    public void publicar(CompraTerminadaSamEvento evento) {
        log.info("Publicando en {} -> compra {}", colaSam, evento.getNumeroCompra());
        jmsTemplate.convertAndSend(colaSam, evento);
    }
}
