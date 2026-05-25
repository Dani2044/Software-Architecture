package com.sps.sam.service;

import com.sps.sam.dto.CompraTerminadaSamDto;
import com.sps.sam.entity.ServicioMedico;
import com.sps.sam.repository.RepoSAM;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio responsable de registrar los servicios medicos
 * adquiridos en la agenda del paciente.
 *
 * <p>Implementa un patron de <b>registro idempotente</b>: si un mensaje
 * de compra llega mas de una vez (reentrega por parte de RabbitMQ),
 * los servicios que ya fueron registrados se omiten sin generar error.
 * La idempotencia se garantiza a dos niveles:</p>
 * <ol>
 *   <li><b>Nivel de aplicacion:</b> antes de insertar, se verifica con
 *       {@link RepoSAM#existsByNumeroCompraAndCodigoServicio}
 *       si la combinacion (numeroCompra, codigoServicio) ya existe.</li>
 *   <li><b>Nivel de base de datos:</b> la tabla {@code agenda_servicio}
 *       posee una restriccion {@code UNIQUE} sobre las mismas columnas,
 *       actuando como red de seguridad ante condiciones de carrera.</li>
 * </ol>
 *
 * @author SPS Team
 * @see com.sps.sam.listener.ListenerSAM
 * @see com.sps.sam.entity.ServicioMedico
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvSAM {

    private final RepoSAM repository;

    /**
     * Registra en la agenda todos los servicios medicos contenidos en un
     * evento de compra finalizada.
     *
     * <p>El metodo itera sobre la lista de servicios del evento y, para cada uno,
     * verifica si ya fue registrado previamente. Solo los servicios nuevos
     * se persisten. La operacion completa se ejecuta dentro de una
     * transaccion unica para garantizar atomicidad.</p>
     *
     * @param evento DTO con la informacion de la compra finalizada,
     *               incluyendo numero de compra, cedula del paciente
     *               y la lista de servicios medicos adquiridos.
     *               Si {@code evento.getServicios()} es {@code null},
     *               el metodo retorna sin realizar ninguna accion.
     */
    @Transactional
    public void registrarCompra(CompraTerminadaSamDto evento) {
        // Si no hay servicios en el evento, no hay nada que registrar
        if (evento.getServicios() == null) return;

        evento.getServicios().forEach(s -> {
            // Verificacion de idempotencia: el mismo (numeroCompra, codigoServicio)
            // no se inserta dos veces. Esto protege contra reentregas de mensajes JMS.
            if (repository.existsByNumeroCompraAndCodigoServicio(evento.getNumeroCompra(), s.getCodigo())) {
                log.info("Servicio {} de compra {} ya registrado, se omite",
                        s.getCodigo(), evento.getNumeroCompra());
                return; // Continua con el siguiente servicio (no sale del forEach)
            }
            // Construye y persiste la entidad con los datos del servicio
            repository.save(ServicioMedico.builder()
                    .numeroCompra(evento.getNumeroCompra())
                    .cedulaCliente(evento.getCedulaCliente())
                    .codigoServicio(s.getCodigo())
                    .nombreServicio(s.getNombre())
                    .duracionMinutos(s.getDuracionMinutos())
                    .build());
        });

        log.info("Agenda registrada para compra {} ({} servicios)",
                evento.getNumeroCompra(), evento.getServicios().size());
    }
}
