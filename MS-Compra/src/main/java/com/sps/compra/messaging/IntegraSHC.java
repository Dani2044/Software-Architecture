package com.sps.compra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegraSHC {

    private final JmsTemplate jmsTemplate;

    @Value("${sps.cola.shc:ColaSHC}")
    private String colaShc;

    public void publicar(CompraTerminadaShcEvento evento) {
        log.info("Publicando en {} -> compra {}", colaShc, evento.getNumeroCompra());
        jmsTemplate.convertAndSend(colaShc, evento);
    }
}
