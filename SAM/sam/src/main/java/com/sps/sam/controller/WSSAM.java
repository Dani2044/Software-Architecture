package com.sps.sam.controller;

import com.sps.sam.entity.AgendaServicio;
import com.sps.sam.repository.RepoSAM;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST que expone los endpoints de consulta del microservicio SAM.
 *
 * <p>Todos los endpoints se sirven bajo el prefijo {@code /api/sam}.
 * Proporciona tres operaciones de solo lectura:</p>
 * <ul>
 *   <li><b>Health check</b> ({@code GET /api/sam/health}) - verificacion de estado
 *       para el balanceador de carga y monitoreo.</li>
 *   <li><b>Consulta por cedula</b> ({@code GET /api/sam/agenda/{cedula}}) -
 *       obtiene todos los servicios agendados de un paciente.</li>
 *   <li><b>Consulta por compra</b> ({@code GET /api/sam/agenda/compra/{numeroCompra}}) -
 *       obtiene los servicios asociados a una compra especifica.</li>
 * </ul>
 *
 * <p>Nota: la escritura de datos no se realiza a traves de este controlador,
 * sino mediante el listener JMS {@link com.sps.sam.listener.ListenerSAM}
 * que consume mensajes de la cola {@code ColaSAM}.</p>
 *
 * @author SPS Team
 * @see com.sps.sam.repository.RepoSAM
 */
@RestController
@RequestMapping("/api/sam")
@RequiredArgsConstructor
public class WSSAM {

    private final RepoSAM repository;

    /**
     * Endpoint de verificacion de salud del microservicio.
     *
     * <p>Retorna un mapa JSON con el estado {@code "UP"} y el nombre del
     * servicio {@code "SAM"}. Utilizado por el balanceador de carga,
     * Docker health checks y herramientas de monitoreo para validar
     * que el microservicio esta operativo.</p>
     *
     * @return mapa con claves {@code "status"} y {@code "service"}
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "SAM");
    }

    /**
     * Consulta todos los servicios medicos agendados para un paciente
     * identificado por su cedula.
     *
     * @param cedula cedula de identidad del paciente
     * @return lista de entidades {@link AgendaServicio} asociadas a esa cedula;
     *         lista vacia si el paciente no tiene servicios agendados
     */
    @GetMapping("/agenda/{cedula}")
    public List<AgendaServicio> porCedula(@PathVariable String cedula) {
        return repository.findByCedulaCliente(cedula);
    }

    /**
     * Consulta todos los servicios medicos agendados que pertenecen a
     * un numero de compra especifico.
     *
     * @param numeroCompra numero unico de la compra en el sistema SPS
     * @return lista de entidades {@link AgendaServicio} asociadas a esa compra;
     *         lista vacia si la compra no tiene servicios registrados
     */
    @GetMapping("/agenda/compra/{numeroCompra}")
    public List<AgendaServicio> porCompra(@PathVariable Long numeroCompra) {
        return repository.findByNumeroCompra(numeroCompra);
    }
}
