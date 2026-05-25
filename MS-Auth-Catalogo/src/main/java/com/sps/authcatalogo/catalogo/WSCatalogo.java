package com.sps.authcatalogo.catalogo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST que actua como punto de entrada para las operaciones
 * de catalogo y compra desde el SPA.
 *
 * <p>Alineado con el diagrama de despliegue UML, este controller es la
 * "cara" que el {@code ProxyWeb} de la SPS-SPA invoca via HTTP. Delega
 * toda la logica de negocio en {@link SrvCatalogo}, que a su vez decide
 * si la operacion va a la base de datos local (catalogo) o si sale al
 * Balanceador via {@link ProxyCatalogo} (compras).</p>
 *
 * <p>Endpoints expuestos:</p>
 * <ul>
 *   <li>{@code GET /api/catalogo/health}        — health check del modulo.</li>
 *   <li>{@code GET /api/catalogo/planes}        — lista todos los planes locales.</li>
 *   <li>{@code GET /api/catalogo/planes/{codigo}} — plan por codigo.</li>
 *   <li>{@code GET /api/catalogo/servicios}     — lista servicios medicos locales.</li>
 *   <li>{@code POST /api/compra}                — crea una compra (reenviada al Balanceador).</li>
 *   <li>{@code GET /api/compra/{numeroCompra}}  — consulta estado de la compra.</li>
 *   <li>{@code GET /api/compra/**}              — variante con path adicional.</li>
 *   <li>{@code POST /api/compra/**}             — variante con path adicional.</li>
 * </ul>
 *
 * @author SPS Team
 * @see SrvCatalogo
 * @see ProxyCatalogo
 */
@RestController
@RequiredArgsConstructor
public class WSCatalogo {

    private final SrvCatalogo srvCatalogo;

    // ─────────────────────────────────────────────
    //  CATALOGO — datos locales
    // ─────────────────────────────────────────────

    @GetMapping("/api/catalogo/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "MS-Auth-Catalogo");
    }

    @GetMapping("/api/catalogo/planes")
    public List<PlanSalud> planes() {
        return srvCatalogo.listarPlanes();
    }

    @GetMapping("/api/catalogo/planes/{codigo}")
    public ResponseEntity<PlanSalud> plan(@PathVariable String codigo) {
        return srvCatalogo.obtenerPlan(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/catalogo/servicios")
    public List<ServicioMedico> servicios() {
        return srvCatalogo.listarServicios();
    }

    // ─────────────────────────────────────────────
    //  COMPRA — delegacion al Balanceador via SrvCatalogo + ProxyCatalogo
    // ─────────────────────────────────────────────

    @PostMapping(value = {"/api/compra", "/api/compra/"})
    public ResponseEntity<String> crearCompra(@RequestBody Object body) {
        try {
            String response = srvCatalogo.crearCompra(body);
            return ResponseEntity.ok(response);
        } catch (ProxyCatalogo.ProxyCatalogoException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        }
    }

    @PostMapping("/api/compra/**")
    public ResponseEntity<String> crearCompraSubpath(@RequestBody Object body,
                                                     HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Extraer el sub-path despues de /api/compra
        String subPath = uri.substring("/api/compra".length());
        try {
            String response = srvCatalogo.crearCompraSubpath(subPath, body);
            return ResponseEntity.ok(response);
        } catch (ProxyCatalogo.ProxyCatalogoException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        }
    }

    @GetMapping("/api/compra/{numeroCompra}")
    public ResponseEntity<String> consultarEstado(@PathVariable long numeroCompra) {
        try {
            String response = srvCatalogo.consultarEstadoCompra(numeroCompra);
            return ResponseEntity.ok(response);
        } catch (ProxyCatalogo.ProxyCatalogoException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        }
    }

    @GetMapping("/api/compra/**")
    public ResponseEntity<String> consultarSubpath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = (query != null && !query.isBlank()) ? path + "?" + query : path;
        try {
            String response = srvCatalogo.consultarCompraSubpath(fullPath);
            return ResponseEntity.ok(response);
        } catch (ProxyCatalogo.ProxyCatalogoException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getBody());
        }
    }
}
