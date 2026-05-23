package com.sps.authcatalogo.catalogo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST que expone el catalogo de planes de salud y servicios medicos.
 *
 * <p>Rutas disponibles bajo el prefijo {@code /api/catalogo}:</p>
 * <ul>
 *   <li>{@code GET /health}           - Verificacion de salud del microservicio.</li>
 *   <li>{@code GET /planes}           - Lista todos los planes de salud con sus servicios asociados.</li>
 *   <li>{@code GET /planes/{codigo}}  - Obtiene un plan especifico por su codigo de negocio.</li>
 *   <li>{@code GET /servicios}        - Lista todos los servicios medicos disponibles.</li>
 * </ul>
 *
 * <p>Todos los endpoints son de solo lectura (GET) y no requieren autenticacion,
 * ya que el catalogo es informacion publica consultada por el frontend SPS-SPA.</p>
 *
 * <p>La logica de negocio se delega a {@link SrvCatalogo}.</p>
 *
 * @see PlanSalud
 * @see ServicioMedico
 * @see SrvCatalogo
 */
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final SrvCatalogo srvCatalogo;

    /**
     * Endpoint de verificacion de salud (health check) del microservicio.
     *
     * @return mapa con el estado ({@code "UP"}) y el nombre del servicio
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "MS-Auth-Catalogo");
    }

    /**
     * Retorna la lista completa de planes de salud disponibles, incluyendo
     * los servicios medicos asociados a cada plan.
     *
     * @return lista de todos los {@link PlanSalud} registrados en el sistema
     */
    @GetMapping("/planes")
    public List<PlanSalud> planes() {
        return srvCatalogo.listarPlanes();
    }

    /**
     * Busca y retorna un plan de salud especifico por su codigo de negocio.
     *
     * @param codigo codigo unico del plan (ej. {@code "PLAN-BASICO-001"})
     * @return el {@link PlanSalud} correspondiente
     * @throws java.util.NoSuchElementException si no se encuentra un plan con el codigo dado
     */
    @GetMapping("/planes/{codigo}")
    public PlanSalud plan(@PathVariable String codigo) {
        return srvCatalogo.obtenerPlan(codigo).orElseThrow();
    }

    /**
     * Retorna la lista completa de servicios medicos disponibles en el catalogo.
     *
     * @return lista de todos los {@link ServicioMedico} registrados en el sistema
     */
    @GetMapping("/servicios")
    public List<ServicioMedico> servicios() {
        return srvCatalogo.listarServicios();
    }
}
