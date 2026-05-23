package com.sps.shc.listener;

import com.sps.shc.dto.CompraTerminadaShcDto;
import com.sps.shc.service.HistoriaClinicaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener JMS que consume mensajes de la cola {@code cola.shc} de ActiveMQ.
 *
 * <p>Este componente es el punto de entrada asíncrono del microservicio SHC.
 * Cada vez que el microservicio MS-Compra finaliza una compra, publica un mensaje
 * en la cola {@code cola.shc}. Este listener lo deserializa automáticamente
 * en un {@link CompraTerminadaShcDto} (gracias al convertidor Jackson configurado
 * en {@link com.sps.shc.config.JmsConfig}) y delega el procesamiento al
 * {@link HistoriaClinicaService}.</p>
 *
 * <p>El nombre de la cola es configurable mediante la propiedad
 * {@code sps.cola.shc}, con valor por defecto {@code cola.shc}.</p>
 *
 * @see com.sps.shc.config.JmsConfig
 * @see com.sps.shc.service.HistoriaClinicaService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShcListener {

    private final HistoriaClinicaService service;

    /**
     * Procesa un mensaje de compra terminada recibido desde la cola JMS.
     *
     * <p>Registra el evento en los logs y delega la lógica de negocio
     * (creación de historias clínicas) al servicio correspondiente.</p>
     *
     * @param evento DTO deserializado con los datos de la compra terminada
     */
    @JmsListener(destination = "${sps.cola.shc:cola.shc}")
    public void onCompraTerminada(CompraTerminadaShcDto evento) {
        log.info("[cola.shc] Mensaje recibido: compra={}", evento.getNumeroCompra());
        service.registrarCompra(evento);
    }
}
