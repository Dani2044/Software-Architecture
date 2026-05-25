package co.sps.balanceador.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import co.sps.balanceador.config.BalanceadorProperties;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadBalancerLogic {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancerLogic.class);

    private final BalanceadorProperties props;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final WebClient.Builder webClientBuilder;
    private final LogService logService;

    public LoadBalancerLogic(BalanceadorProperties props,
                              WebClient.Builder webClientBuilder,
                              LogService logService) {
        this.props = props;
        this.webClientBuilder = webClientBuilder;
        this.logService = logService;
    }

    public String nextBackend() {
        int index = Math.abs(counter.getAndIncrement() % props.getBackends().size());
        String selected = props.getBackends().get(index);
        log.debug("LoadBalancer selecciono backend [{}]: {}", index, selected);
        return selected;
    }

    public Mono<String> get(String path) {
        return Mono.defer(() -> {
            String base = nextBackend();
            log.info("Forwarding GET {} -> {}{}", path, base, path);
            logService.registrar("REQUEST", "GET", base, path, "Solicitud enviada");

            return webClientBuilder.baseUrl(base).build()
                    .get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(body ->
                        logService.registrar("RESPONSE", "GET", base, path, "Respuesta exitosa")
                    )
                    .doOnError(error ->
                        logService.registrar("ERROR", "GET", base, path, error.getMessage())
                    );
        })
        .retryWhen(
            Retry.max(props.getBackends().size() - 1)
                 .filter(ex -> ex instanceof WebClientResponseException
                         || ex instanceof java.util.concurrent.TimeoutException)
                 .doBeforeRetry(sig -> {
                     log.warn("Reintentando tras error: {}", sig.failure().getMessage());
                     logService.registrar("RETRY", "GET", "N/A", path,
                             "Reintento #" + sig.totalRetries() + " — " + sig.failure().getMessage());
                 })
        );
    }

    /**
     * Reenvia un GET a un backend fijo (sin round-robin).
     * Usado para rutas que van siempre al mismo microservicio (auth, catalogo).
     */
    public Mono<String> getDirect(String baseUrl, String path) {
        log.info("Forwarding GET {} -> {}{}", path, baseUrl, path);
        logService.registrar("REQUEST", "GET", baseUrl, path, "Solicitud enviada (direct)");

        return webClientBuilder.baseUrl(baseUrl).build()
                .get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(body ->
                    logService.registrar("RESPONSE", "GET", baseUrl, path, "Respuesta exitosa")
                )
                .doOnError(error ->
                    logService.registrar("ERROR", "GET", baseUrl, path, error.getMessage())
                );
    }

    /**
     * Reenvia un POST a un backend fijo (sin round-robin).
     * Usado para rutas que van siempre al mismo microservicio (auth, catalogo).
     */
    public Mono<String> postDirect(String baseUrl, String path, Object body) {
        log.info("Forwarding POST {} -> {}{}", path, baseUrl, path);
        logService.registrar("REQUEST", "POST", baseUrl, path, "Solicitud enviada (direct)");

        return webClientBuilder.baseUrl(baseUrl).build()
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(res ->
                    logService.registrar("RESPONSE", "POST", baseUrl, path, "Respuesta exitosa")
                )
                .doOnError(error ->
                    logService.registrar("ERROR", "POST", baseUrl, path, error.getMessage())
                );
    }

    public Mono<String> post(String path, Object body) {
        return Mono.defer(() -> {
            String base = nextBackend();
            log.info("Forwarding POST {} -> {}{}", path, base, path);
            logService.registrar("REQUEST", "POST", base, path, "Solicitud enviada");

            return webClientBuilder.baseUrl(base).build()
                    .post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(res ->
                        logService.registrar("RESPONSE", "POST", base, path, "Respuesta exitosa")
                    )
                    .doOnError(error ->
                        logService.registrar("ERROR", "POST", base, path, error.getMessage())
                    );
        })
        .retryWhen(
            Retry.max(props.getBackends().size() - 1)
                 .filter(ex -> ex instanceof WebClientResponseException
                         || ex instanceof java.util.concurrent.TimeoutException)
                 .doBeforeRetry(sig -> {
                     log.warn("Reintentando tras error: {}", sig.failure().getMessage());
                     logService.registrar("RETRY", "POST", "N/A", path,
                             "Reintento #" + sig.totalRetries() + " — " + sig.failure().getMessage());
                 })
        );
    }
}
