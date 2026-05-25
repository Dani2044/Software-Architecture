package co.sps.balanceador.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import co.sps.balanceador.service.LoadBalancerLogic;
import co.sps.balanceador.service.SrvRegistryInterface;
import reactor.core.publisher.Mono;

/**
 * IP virtual (load balancer) del sistema SPS para las peticiones de compra.
 *
 * <p>Construido en Spring WebFlux, distribuye las solicitudes que llegan
 * bajo {@code /api/compra/**} entre las replicas activas de MS-Compra
 * mediante round-robin (ver {@link LoadBalancerLogic}).</p>
 *
 * <p><b>Importante:</b> este componente <i>no</i> es un gateway general del
 * sistema. Es invocado por {@code ProxyCatalogo} del microservicio
 * MS-Auth-Catalogo, no directamente por las SPAs. Tal como muestra el
 * diagrama de despliegue UML, el flujo de compra es:</p>
 * <pre>
 *   SPS-SPA (ProxyWeb) -> MS-Auth-Catalogo (ProxyCatalogo) -> Balanceador (WSIPVirtual) -> MS-Compra
 * </pre>
 *
 * @author SPS Team
 */
@RestController
@RequestMapping("/api/compra")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class WSIPVirtual {

    private static final Logger log = LoggerFactory.getLogger(WSIPVirtual.class);

    private final LoadBalancerLogic lb;
    private final SrvRegistryInterface registry;

    public WSIPVirtual(LoadBalancerLogic lb, SrvRegistryInterface registry) {
        this.lb = lb;
        this.registry = registry;
    }

    /**
     * Endpoint local del balanceador (no se reenvia): devuelve el registro
     * de backends activos. Utilizado para diagnostico/monitoreo.
     */
    @GetMapping("/registry")
    public ResponseEntity<?> getRegistry() {
        return ResponseEntity.ok(registry.getAvailableBackends());
    }

    /**
     * POST /api/compra (sin path adicional) -> round-robin a MS-Compra.
     */
    @PostMapping(value = {"", "/"})
    public Mono<ResponseEntity<String>> forwardCompraPostRoot(@RequestBody Object body) {
        String fullPath = "/api/compra";
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    /**
     * POST /api/compra/** -> round-robin a MS-Compra preservando el path completo.
     */
    @PostMapping("/**")
    public Mono<ResponseEntity<String>> forwardCompraPost(@RequestBody Object body,
                                                          ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    /**
     * GET /api/compra/** -> round-robin a MS-Compra preservando path y query string.
     */
    @GetMapping("/**")
    public Mono<ResponseEntity<String>> forwardCompraGet(ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        String query = exchange.getRequest().getURI().getRawQuery();
        if (query != null && !query.isEmpty()) {
            fullPath = fullPath + "?" + query;
        }
        log.info("WSIPVirtual -> GET {}", fullPath);
        return lb.get(fullPath)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }
}
