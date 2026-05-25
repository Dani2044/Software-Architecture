package com.sps.authcatalogo.catalogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST que actua como proxy/fachada de catalogo y orquestador
 * de compras en el sistema SPS.
 *
 * <p>Alineado con el diagrama de despliegue UML, este componente es el unico
 * punto de entrada que las SPAs (a traves de {@code ProxyWeb}) utilizan para
 * operaciones de catalogo y compra. El flujo de compra es:</p>
 * <pre>
 *   SPS-SPA (ProxyWeb) -> ProxyCatalogo -> WSIPVirtual (Balanceador) -> MS-Compra
 * </pre>
 *
 * <p>Responsabilidades:</p>
 * <ol>
 *   <li><b>Catalogo (local):</b> servir planes y servicios medicos desde la BD
 *       local del modulo, delegando en {@link SrvCatalogo}.</li>
 *   <li><b>Compra (proxy al Balanceador):</b> reenviar las peticiones
 *       {@code /api/compra/**} al {@code WSIPVirtual} del Balanceador,
 *       que aplica round-robin sobre las replicas de MS-Compra.</li>
 * </ol>
 *
 * @see SrvCatalogo
 * @see com.sps.authcatalogo.config.WebClientConfig
 */
@RestController
public class ProxyCatalogo {

    private static final Logger log = LoggerFactory.getLogger(ProxyCatalogo.class);

    private final SrvCatalogo srvCatalogo;
    private final RestClient balanceadorRestClient;

    public ProxyCatalogo(SrvCatalogo srvCatalogo,
                         @Qualifier("balanceadorRestClient") RestClient balanceadorRestClient) {
        this.srvCatalogo = srvCatalogo;
        this.balanceadorRestClient = balanceadorRestClient;
    }

    // ─────────────────────────────────────────────
    //  CATALOGO — endpoints locales (BD MS-Auth-Catalogo)
    // ─────────────────────────────────────────────

    /**
     * Endpoint de verificacion de salud (health check) del microservicio.
     */
    @GetMapping("/api/catalogo/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "MS-Auth-Catalogo");
    }

    /**
     * Retorna la lista completa de planes de salud disponibles.
     */
    @GetMapping("/api/catalogo/planes")
    public List<PlanSalud> planes() {
        return srvCatalogo.listarPlanes();
    }

    /**
     * Busca y retorna un plan de salud especifico por su codigo de negocio.
     */
    @GetMapping("/api/catalogo/planes/{codigo}")
    public ResponseEntity<PlanSalud> plan(@PathVariable String codigo) {
        return srvCatalogo.obtenerPlan(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retorna la lista completa de servicios medicos disponibles en el catalogo.
     */
    @GetMapping("/api/catalogo/servicios")
    public List<ServicioMedico> servicios() {
        return srvCatalogo.listarServicios();
    }

    // ─────────────────────────────────────────────
    //  COMPRA — proxy al Balanceador (WSIPVirtual)
    // ─────────────────────────────────────────────

    /**
     * Reenvia POST /api/compra al WSIPVirtual del Balanceador.
     * El Balanceador a su vez aplica round-robin sobre las replicas de MS-Compra.
     */
    @PostMapping(value = {"/api/compra", "/api/compra/"})
    public ResponseEntity<String> forwardCompraPost(@RequestBody Object body) {
        return forwardPost("/api/compra", body);
    }

    /**
     * Reenvia POST /api/compra/** al WSIPVirtual del Balanceador.
     */
    @PostMapping("/api/compra/**")
    public ResponseEntity<String> forwardCompraPostSubpath(@RequestBody Object body,
                                                           HttpServletRequest request) {
        return forwardPost(request.getRequestURI(), body);
    }

    /**
     * Reenvia GET /api/compra/** al WSIPVirtual del Balanceador.
     * Usado por el SPA para el polling de estado de la compra.
     */
    @GetMapping("/api/compra/**")
    public ResponseEntity<String> forwardCompraGet(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = (query != null && !query.isBlank()) ? path + "?" + query : path;
        return forwardGet(fullPath);
    }

    // ─────────────────────────────────────────────
    //  Helpers de reenvio
    // ─────────────────────────────────────────────

    private ResponseEntity<String> forwardPost(String path, Object body) {
        log.info("ProxyCatalogo -> Balanceador POST {}", path);
        try {
            String response = balanceadorRestClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(response);
        } catch (RestClientResponseException ex) {
            log.warn("Balanceador respondio {}: {}", ex.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error reenviando al Balanceador: {}", ex.getMessage());
            return ResponseEntity.status(503).body("{\"error\":\"Balanceador no disponible\"}");
        }
    }

    private ResponseEntity<String> forwardGet(String path) {
        log.info("ProxyCatalogo -> Balanceador GET {}", path);
        try {
            String response = balanceadorRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok(response);
        } catch (RestClientResponseException ex) {
            log.warn("Balanceador respondio {}: {}", ex.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error reenviando al Balanceador: {}", ex.getMessage());
            return ResponseEntity.status(503).body("{\"error\":\"Balanceador no disponible\"}");
        }
    }
}
