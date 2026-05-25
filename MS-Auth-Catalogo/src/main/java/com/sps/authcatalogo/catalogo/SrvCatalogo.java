package com.sps.authcatalogo.catalogo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de catalogo y compras del modulo MS-Auth-Catalogo.
 *
 * <p>Es el orquestador de la logica de negocio del modulo. Decide cuando ir
 * a la base de datos local (catalogo de planes/servicios) y cuando salir al
 * Balanceador (operaciones de compra). Alineado con el diagrama de
 * despliegue UML donde {@code SrvCatalogo} es invocado por el controller y
 * a su vez utiliza {@link RepoCatalogo} y {@link ProxyCatalogo}.</p>
 *
 * <p>Colaboradores:</p>
 * <ul>
 *   <li>{@link RepoCatalogo} — datos locales (planes de salud).</li>
 *   <li>{@link ServicioMedicoRepository} — datos locales (servicios medicos).</li>
 *   <li>{@link ProxyCatalogo} — cliente HTTP al Balanceador para compras.</li>
 * </ul>
 *
 * @author SPS Team
 * @see RepoCatalogo
 * @see ServicioMedicoRepository
 * @see ProxyCatalogo
 */
@Service
@RequiredArgsConstructor
public class SrvCatalogo {

    private final RepoCatalogo repoCatalogo;
    private final ServicioMedicoRepository servicioRepository;
    private final ProxyCatalogo proxyCatalogo;

    // ─────────────────────────────────────────────
    //  CATALOGO — datos locales
    // ─────────────────────────────────────────────

    /**
     * Retorna la lista completa de planes de salud disponibles.
     */
    public List<PlanSalud> listarPlanes() {
        return repoCatalogo.findAll();
    }

    /**
     * Busca un plan de salud especifico por su codigo de negocio.
     */
    public Optional<PlanSalud> obtenerPlan(String codigo) {
        return repoCatalogo.findByCodigo(codigo);
    }

    /**
     * Busca un plan de salud especifico por su identificador numerico.
     */
    public Optional<PlanSalud> obtenerPlan(Long id) {
        return repoCatalogo.findById(id);
    }

    /**
     * Retorna la lista completa de servicios medicos disponibles en el catalogo.
     */
    public List<ServicioMedico> listarServicios() {
        return servicioRepository.findAll();
    }

    // ─────────────────────────────────────────────
    //  COMPRA — delega al Balanceador via ProxyCatalogo
    // ─────────────────────────────────────────────

    /**
     * Crea una nueva compra delegando al Balanceador via {@link ProxyCatalogo}.
     *
     * @param body datos de la compra
     * @return respuesta JSON del Balanceador (numeroCompra, estado, mensaje)
     */
    public String crearCompra(Object body) {
        return proxyCatalogo.crearCompra(body);
    }

    /**
     * Variante con sub-path arbitrario bajo /api/compra.
     */
    public String crearCompraSubpath(String subPath, Object body) {
        return proxyCatalogo.crearCompraSubpath(subPath, body);
    }

    /**
     * Consulta el estado de una compra existente.
     *
     * @param numeroCompra identificador unico de la compra
     * @return respuesta JSON del Balanceador con el estado actual
     */
    public String consultarEstadoCompra(long numeroCompra) {
        return proxyCatalogo.consultarEstado(numeroCompra);
    }

    /**
     * Variante para sub-paths arbitrarios (incluyendo query string).
     */
    public String consultarCompraSubpath(String fullPath) {
        return proxyCatalogo.consultarSubpath(fullPath);
    }
}
