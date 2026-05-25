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
 * Controller del balanceador que actua como IP virtual hacia las replicas de MS-Compra.
 *
 * <p>Recibe TODAS las peticiones bajo {@code /api/compra/**} y las reenvia
 * a una de las replicas (round-robin) preservando el path completo, de modo
 * que la URL del backend coincide con la URL recibida.</p>
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
     * Endpoint local que NO se reenvia: devuelve el registro de backends activos.
     */
    @GetMapping("/registry")
    public ResponseEntity<?> getRegistry() {
        return ResponseEntity.ok(registry.getAvailableBackends());
    }

    /**
     * POST /api/compra (sin path adicional) → reenviado tal cual a MS-Compra.
     * Lo usa el SPA para crear una nueva compra.
     */
    @PostMapping(value = {"", "/"})
    public Mono<ResponseEntity<String>> forwardPostRoot(@RequestBody Object body) {
        String fullPath = "/api/compra";
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    /**
     * POST /api/compra/** → reenviado preservando el path completo.
     */
    @PostMapping("/**")
    public Mono<ResponseEntity<String>> forwardPost(@RequestBody Object body,
                                                    ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    /**
     * GET /api/compra/** → reenviado preservando el path completo y query string.
     */
    @GetMapping("/**")
    public Mono<ResponseEntity<String>> forwardGet(ServerWebExchange exchange) {
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
