package com.sps.compra.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Adaptador de mensajeria que publica eventos de compra terminada
 * en la cola {@code ColaSAM} de RabbitMQ para el microservicio SAM.
 *
 * <p>Usa el exchange por defecto (direct {@code ""}) donde el routing key
 * coincide con el nombre de la cola destino.</p>
 *
 * @see com.sps.compra.config.RabbitMQConfiguration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegraSAM {

    private final RabbitTemplate rabbitTemplate;

    @Value("${sps.cola.sam:ColaSAM}")
    private String colaSam;

    public void publicar(CompraTerminadaSamEvento evento) {
        log.info("Publicando en {} -> compra {}", colaSam, evento.getNumeroCompra());
        rabbitTemplate.convertAndSend(colaSam, evento);
    }
}
