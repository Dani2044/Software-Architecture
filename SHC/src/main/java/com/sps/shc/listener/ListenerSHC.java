package com.sps.shc.listener;

import com.sps.shc.dto.CompraTerminadaShcDto;
import com.sps.shc.service.SrvSHC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener JMS que consume mensajes de la cola {@code ColaSHC} de ActiveMQ.
 *
 * <p>Este componente es el punto de entrada asíncrono del microservicio SHC.
 * Cada vez que el microservicio MS-Compra finaliza una compra, publica un mensaje
 * en la cola {@code ColaSHC}. Este listener lo deserializa automáticamente
 * en un {@link CompraTerminadaShcDto} (gracias al convertidor Jackson configurado
 * en {@link com.sps.shc.config.JmsConfig}) y delega el procesamiento al
 * {@link SrvSHC}.</p>
 *
 * <p>El nombre de la cola es configurable mediante la propiedad
 * {@code sps.ColaSHC}, con valor por defecto {@code ColaSHC}.</p>
 *
 * @see com.sps.shc.config.JmsConfig
 * @see com.sps.shc.service.SrvSHC
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerSHC {

    private final SrvSHC service;

    /**
     * Procesa un mensaje de compra terminada recibido desde la cola JMS.
     *
     * <p>Registra el evento en los logs y delega la lógica de negocio
     * (creación de historias clínicas) al servicio correspondiente.</p>
     *
     * @param evento DTO deserializado con los datos de la compra terminada
     */
    @JmsListener(destination = "${sps.ColaSHC:ColaSHC}")
    public void onCompraTerminada(CompraTerminadaShcDto evento) {
        log.info("[ColaSHC] Mensaje recibido: compra={}", evento.getNumeroCompra());
        service.registrarCompra(evento);
    }
}
