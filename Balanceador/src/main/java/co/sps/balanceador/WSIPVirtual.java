package co.sps.balanceador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/compra")
public class WSIPVirtual {

    private static final Logger log = LoggerFactory.getLogger(WSIPVirtual.class);

    private final LoadBalancerLogic lb;
    private final SrvRegistryInterface registry;

    public WSIPVirtual(LoadBalancerLogic lb, SrvRegistryInterface registry) {
        this.lb = lb;
        this.registry = registry;
    }

    @GetMapping("/{path}")
    public Mono<ResponseEntity<String>> forwardGet(@PathVariable String path,
                                                   @RequestParam(required = false) String query) {
        String fullPath = "/" + path + (query != null ? "?" + query : "");
        log.info("WSIPVirtual -> GET {}", fullPath);
        return lb.get(fullPath)
                 .map(body -> ResponseEntity.ok(body))
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    @PostMapping("/{path}")
    public Mono<ResponseEntity<String>> forwardPost(@PathVariable String path,
                                                    @RequestBody Object body) {
        String fullPath = "/" + path;
        log.info("WSIPVirtual -> POST {}", fullPath);
        return lb.post(fullPath, body)
                 .map(res -> ResponseEntity.ok(res))
                 .onErrorReturn(ResponseEntity.status(503).body("{\"error\":\"Servicio no disponible\"}"));
    }

    @GetMapping("/registry")
    public ResponseEntity<?> getRegistry() {
        return ResponseEntity.ok(registry.getAvailableBackends());
    }
}
