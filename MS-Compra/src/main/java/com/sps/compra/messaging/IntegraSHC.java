package com.sps.compra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Adaptador de mensajeria que publica eventos de compra terminada
 * en la cola {@code ColaSHC} de RabbitMQ para el microservicio SHC.
 *
 * <p>Usa el exchange por defecto (direct {@code ""}) donde el routing key
 * coincide con el nombre de la cola destino.</p>
 *
 * @see com.sps.compra.config.RabbitMQConfiguration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegraSHC {

    private final RabbitTemplate rabbitTemplate;

    @Value("${sps.cola.shc:ColaSHC}")
    private String colaShc;

    public void publicar(CompraTerminadaShcEvento evento) {
        log.info("Publicando en {} -> compra {}", colaShc, evento.getNumeroCompra());
        rabbitTemplate.convertAndSend(colaShc, evento);
    }
}
