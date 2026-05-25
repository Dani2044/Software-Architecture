package co.sps.balanceador.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import co.sps.balanceador.config.BalanceadorProperties;
import co.sps.balanceador.service.LoadBalancerLogic;
import co.sps.balanceador.service.SrvRegistryInterface;
import reactor.core.publisher.Mono;

/**
 * Gateway (IP virtual) del sistema SPS.
 *
 * <p>Actua como equivalente a un NGINX reverse-proxy construido en Spring WebFlux.
 * Recibe TODAS las peticiones bajo {@code /api/**} y las enruta al microservicio
 * correspondiente segun el prefijo del path:</p>
 * <ul>
 *   <li>{@code /api/compra/**}   — round-robin hacia las replicas de MS-Compra.</li>
 *   <li>{@code /api/auth/**}     — forward directo a MS-Auth-Catalogo.</li>
 *   <li>{@code /api/catalogo/**} — forward directo a MS-Auth-Catalogo.</li>
 * </ul>
 *
 * <p>Esto alinea el sistema con el diagrama de despliegue UML donde el Balanceador
 * es el unico punto de entrada (gateway) y las SPAs nunca contactan directamente
 * a los microservicios backend.</p>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class WSIPVirtual {

    private static final Logger log = LoggerFactory.getLogger(WSIPVirtual.class);

    private final LoadBalancerLogic lb;
    private final SrvRegistryInterface registry;
    private final String authCatalogoUrl;

    public WSIPVirtual(LoadBalancerLogic lb,
                       SrvRegistryInterface registry,
                       BalanceadorProperties props) {
        this.lb = lb;
        this.registry = registry;
        this.authCatalogoUrl = props.getAuthCatalogoUrl();
        log.info("WSIPVirtual inicializado — auth-catalogo-url: {}", authCatalogoUrl);
    }

    // ─────────────────────────────────────────────
    //  COMPRA — round-robin a replicas de MS-Compra
    // ─────────────────────────────────────────────

    /**
     * Endpoint local que NO se reenvia: devuelve el registro de backends activos.
     */
    @GetMapping("/compra/registry")
    public ResponseEntity<?> getRegistry() {
        return ResponseEntity.ok(registry.getAvailableBackends());
    }

    /**
     * POST /api/compra (sin path adicional) → reenviado tal cual a MS-Compra.
     * Lo usa el SPA para crear una nueva compra.
     */
    @PostMapping(value = {"/compra", "/compra/"})
    public Mono<ResponseEntity<String>> forwardCompraPostRoot(@RequestBody Object body) {
        String fullPath = "/api/compra";
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    /**
     * POST /api/compra/** → reenviado preservando el path completo.
     */
    @PostMapping("/compra/**")
    public Mono<ResponseEntity<String>> forwardCompraPost(@RequestBody Object body,
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
    @GetMapping("/compra/**")
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

    // ─────────────────────────────────────────────
    //  AUTH — forward directo a MS-Auth-Catalogo
    // ─────────────────────────────────────────────

    /**
     * POST /api/auth/** → forward directo a MS-Auth-Catalogo.
     * Se usa para login y registro.
     */
    @PostMapping("/auth/**")
    public Mono<ResponseEntity<String>> forwardAuthPost(@RequestBody Object body,
                                                        ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        log.info("WSIPVirtual -> POST {} (auth -> {})", fullPath, authCatalogoUrl);
        return lb.postDirect(authCatalogoUrl, fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Auth no disponible\"}"));
    }

    /**
     * GET /api/auth/** → forward directo a MS-Auth-Catalogo.
     */
    @GetMapping("/auth/**")
    public Mono<ResponseEntity<String>> forwardAuthGet(ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        String query = exchange.getRequest().getURI().getRawQuery();
        if (query != null && !query.isEmpty()) {
            fullPath = fullPath + "?" + query;
        }
        log.info("WSIPVirtual -> GET {} (auth -> {})", fullPath, authCatalogoUrl);
        return lb.getDirect(authCatalogoUrl, fullPath)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Auth no disponible\"}"));
    }

    // ─────────────────────────────────────────────
    //  CATALOGO — forward directo a MS-Auth-Catalogo
    // ─────────────────────────────────────────────

    /**
     * POST /api/catalogo/** → forward directo a MS-Auth-Catalogo.
     */
    @PostMapping("/catalogo/**")
    public Mono<ResponseEntity<String>> forwardCatalogoPost(@RequestBody Object body,
                                                            ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        log.info("WSIPVirtual -> POST {} (catalogo -> {})", fullPath, authCatalogoUrl);
        return lb.postDirect(authCatalogoUrl, fullPath, body)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Catalogo no disponible\"}"));
    }

    /**
     * GET /api/catalogo/** → forward directo a MS-Auth-Catalogo.
     * Se usa para obtener planes de salud y servicios medicos.
     */
    @GetMapping("/catalogo/**")
    public Mono<ResponseEntity<String>> forwardCatalogoGet(ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getPath().value();
        String query = exchange.getRequest().getURI().getRawQuery();
        if (query != null && !query.isEmpty()) {
            fullPath = fullPath + "?" + query;
        }
        log.info("WSIPVirtual -> GET {} (catalogo -> {})", fullPath, authCatalogoUrl);
        return lb.getDirect(authCatalogoUrl, fullPath)
                 .map(ResponseEntity::ok)
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Catalogo no disponible\"}"));
    }
}
