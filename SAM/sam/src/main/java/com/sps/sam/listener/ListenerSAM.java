package com.sps.sam.listener;

import com.sps.sam.dto.CompraTerminadaSamDto;
import com.sps.sam.service.SrvSAM;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener AMQP que consume mensajes de la cola {@code ColaSAM} de RabbitMQ.
 *
 * <p>Este componente es el punto de entrada asincrono del microservicio SAM.
 * Cuando el modulo MS-Compra finaliza una compra que incluye servicios medicos,
 * publica un mensaje en la cola. Este listener lo recibe, lo deserializa
 * automaticamente a {@link CompraTerminadaSamDto} (gracias al convertidor
 * Jackson configurado en {@link com.sps.sam.config.RabbitMQConfiguration})
 * y delega el procesamiento al servicio de negocio {@link SrvSAM}.</p>
 *
 * <p>El nombre de la cola se configura mediante la propiedad
 * {@code sps.cola.sam} (por defecto: {@code ColaSAM}).</p>
 *
 * @author SPS Team
 * @see com.sps.sam.service.SrvSAM
 * @see com.sps.sam.config.RabbitMQConfiguration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerSAM {

    private final SrvSAM agendaService;

    /**
     * Procesa un mensaje de compra finalizada recibido desde la cola AMQP.
     *
     * <p>Registra en el log la recepcion del mensaje con el numero de compra
     * y la cedula del cliente, y luego delega al servicio de agenda para
     * persistir los servicios medicos de forma idempotente.</p>
     *
     * @param evento DTO deserializado automaticamente desde el mensaje AMQP,
     *               contiene los datos de la compra y los servicios medicos
     *               a registrar en la agenda
     */
    @RabbitListener(queues = "${sps.cola.sam:ColaSAM}")
    public void onCompraTerminada(CompraTerminadaSamDto evento) {
        log.info("[ColaSAM] Mensaje recibido: compra={} cedula={}",
                evento.getNumeroCompra(), evento.getCedulaCliente());
        agendaService.registrarCompra(evento);
    }
}
