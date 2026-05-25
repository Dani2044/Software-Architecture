package com.sps.shc.listener;

import com.sps.shc.dto.CompraTerminadaShcDto;
import com.sps.shc.service.SrvSHC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener AMQP que consume mensajes de la cola {@code ColaSHC} de RabbitMQ.
 *
 * <p>Este componente es el punto de entrada asincrono del microservicio SHC.
 * Cada vez que el microservicio MS-Compra finaliza una compra, publica un mensaje
 * en la cola {@code ColaSHC}. Este listener lo deserializa automaticamente
 * en un {@link CompraTerminadaShcDto} (gracias al convertidor Jackson configurado
 * en {@link com.sps.shc.config.RabbitMQConfiguration}) y delega el procesamiento
 * al {@link SrvSHC}.</p>
 *
 * <p>El nombre de la cola es configurable mediante la propiedad
 * {@code sps.cola.shc}, con valor por defecto {@code ColaSHC}.</p>
 *
 * @see com.sps.shc.config.RabbitMQConfiguration
 * @see com.sps.shc.service.SrvSHC
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerSHC {

    private final SrvSHC service;

    /**
     * Procesa un mensaje de compra terminada recibido desde la cola AMQP.
     *
     * <p>Registra el evento en los logs y delega la logica de negocio
     * (creacion de historias clinicas) al servicio correspondiente.</p>
     *
     * @param evento DTO deserializado con los datos de la compra terminada
     */
    @RabbitListener(queues = "${sps.cola.shc:ColaSHC}")
    public void onCompraTerminada(CompraTerminadaShcDto evento) {
        log.info("[ColaSHC] Mensaje recibido: compra={}", evento.getNumeroCompra());
        service.registrarCompra(evento);
    }
}
