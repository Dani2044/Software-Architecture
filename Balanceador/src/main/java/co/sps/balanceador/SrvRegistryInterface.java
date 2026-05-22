package co.sps.balanceador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SrvRegistryInterface {

    private static final Logger log = LoggerFactory.getLogger(SrvRegistryInterface.class);

    private final Map<String, String> registry = new ConcurrentHashMap<>();

    public void register(String nodeId, String baseUrl) {
        registry.put(nodeId, baseUrl);
        log.info("Registro de backend: [{}] -> {}", nodeId, baseUrl);
    }

    public void deregister(String nodeId) {
        String removed = registry.remove(nodeId);
        if (removed != null) {
            log.warn("Backend dado de baja: [{}] -> {}", nodeId, removed);
        }
    }

    public List<String> getAvailableBackends() {
        List<String> list = new ArrayList<>(registry.values());
        return Collections.unmodifiableList(list);
    }

    public void initDefaults() {
        register("compra-master", "http://10.43.100.111:8082");
        register("compra-replica1", "http://10.43.99.121:8082");
        register("compra-replica2", "http://10.43.99.121:8083");
        log.info("Registro inicializado con {} backends.", registry.size());
    }
}
