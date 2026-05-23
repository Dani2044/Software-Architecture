package com.sps.sam.listener;

import com.sps.sam.dto.CompraTerminadaSamDto;
import com.sps.sam.service.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener JMS que consume mensajes de la cola {@code ColaSAM} de ActiveMQ.
 *
 * <p>Este componente es el punto de entrada asincrono del microservicio SAM.
 * Cuando el modulo MS-Compra finaliza una compra que incluye servicios medicos,
 * publica un mensaje en la cola. Este listener lo recibe, lo deserializa
 * automaticamente a {@link CompraTerminadaSamDto} (gracias al convertidor
 * Jackson configurado en {@link com.sps.sam.config.JmsConfig}) y delega
 * el procesamiento al servicio de negocio {@link com.sps.sam.service.AgendaService}.</p>
 *
 * <p>El nombre de la cola se configura mediante la propiedad
 * {@code sps.ColaSAM} (por defecto: {@code ColaSAM}).</p>
 *
 * @author SPS Team
 * @see com.sps.sam.service.AgendaService
 * @see com.sps.sam.config.JmsConfig
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SamListener {

    private final AgendaService agendaService;

    /**
     * Procesa un mensaje de compra finalizada recibido desde la cola JMS.
     *
     * <p>Registra en el log la recepcion del mensaje con el numero de compra
     * y la cedula del cliente, y luego delega al servicio de agenda para
     * persistir los servicios medicos de forma idempotente.</p>
     *
     * @param evento DTO deserializado automaticamente desde el mensaje JMS,
     *               contiene los datos de la compra y los servicios medicos
     *               a registrar en la agenda
     */
    @JmsListener(destination = "${sps.ColaSAM:ColaSAM}")
    public void onCompraTerminada(CompraTerminadaSamDto evento) {
        log.info("[ColaSAM] Mensaje recibido: compra={} cedula={}",
                evento.getNumeroCompra(), evento.getCedulaCliente());
        // Delega al servicio de negocio que maneja la logica de idempotencia y persistencia
        agendaService.registrarCompra(evento);
    }
}
