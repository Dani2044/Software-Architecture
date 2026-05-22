package co.sps.balanceador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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

    public LoadBalancerLogic(BalanceadorProperties props, WebClient.Builder webClientBuilder) {
        this.props = props;
        this.webClientBuilder = webClientBuilder;
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
            return webClientBuilder.baseUrl(base).build()
                    .get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10));
        })
        .retryWhen(
            Retry.max(props.getBackends().size() - 1)
                 .filter(ex -> ex instanceof WebClientResponseException
                             || ex instanceof java.util.concurrent.TimeoutException)
                 .doBeforeRetry(sig ->
                     log.warn("Reintentando tras error: {}", sig.failure().getMessage()))
        );
    }

    public Mono<String> post(String path, Object body) {
        return Mono.defer(() -> {
            String base = nextBackend();
            log.info("Forwarding POST {} -> {}{}", path, base, path);
            return webClientBuilder.baseUrl(base).build()
                    .post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30));
        })
        .retryWhen(
            Retry.max(props.getBackends().size() - 1)
                 .filter(ex -> ex instanceof WebClientResponseException
                             || ex instanceof java.util.concurrent.TimeoutException)
        );
    }
}
