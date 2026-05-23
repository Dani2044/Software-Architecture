package co.sps.balanceador.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.List;

@Component
public class HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(HealthCheck.class);
    private static final String HEALTH_PATH = "/actuator/health";

    private static final List<BackendDef> BACKEND_DEFINITIONS = List.of(
            new BackendDef("compra-master",  "http://10.43.100.122:8081"),
            new BackendDef("compra-replica", "http://10.43.99.121:8081")
    );

    private final SrvRegistryInterface registry;
    private final WebClient webClient;
    private final LogService logService;

    public HealthCheck(SrvRegistryInterface registry,
                       WebClient.Builder builder,
                       LogService logService) {
        this.registry = registry;
        this.webClient = builder.build();
        this.logService = logService;
    }

    @PostConstruct
    public void init() {
        registry.initDefaults();
        log.info("HealthCheck inicializado. Primera verificacion al arrancar.");
        logService.registrar("HEALTH_INIT", "HEALTH", "ALL", HEALTH_PATH,
                "Sistema inicializado con " + BACKEND_DEFINITIONS.size() + " backends");
        checkAll();
    }

    @Scheduled(fixedDelayString = "#{balanceadorProperties.healthcheck.intervalMs}")
    public void checkAll() {
        log.debug("HealthCheck verificando {} backends...", BACKEND_DEFINITIONS.size());
        BACKEND_DEFINITIONS.forEach(this::check);
    }

    private void check(BackendDef def) {
        webClient.get()
                .uri(def.baseUrl() + HEALTH_PATH)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        body  -> onUp(def),
                        error -> onDown(def, error)
                );
    }

    private void onUp(BackendDef def) {
        List<String> current = registry.getAvailableBackends();
        if (!current.contains(def.baseUrl())) {
            log.info("Backend recuperado: [{}] -> {}", def.id(), def.baseUrl());
            registry.register(def.id(), def.baseUrl());
            logService.registrar("HEALTH_UP", "HEALTH", def.baseUrl(), HEALTH_PATH,
                    "Backend [" + def.id() + "] recuperado y registrado");
        } else {
            log.debug("Backend activo: [{}] -> {}", def.id(), def.baseUrl());
        }
    }

    private void onDown(BackendDef def, Throwable error) {
        log.warn("Backend no disponible: [{}] -> {} -- {}", def.id(), def.baseUrl(), error.getMessage());
        registry.deregister(def.id());
        logService.registrar("HEALTH_DOWN", "HEALTH", def.baseUrl(), HEALTH_PATH,
                "Backend [" + def.id() + "] caido — " + error.getMessage());
    }

    record BackendDef(String id, String baseUrl) {}
}
