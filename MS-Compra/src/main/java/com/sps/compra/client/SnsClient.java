package com.sps.compra.client;

import com.sps.compra.entity.EstadoValidacionSns;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Cliente asincrono no bloqueante hacia la SNS.
 * El enunciado exige asincronia SIN MOM, asi que usamos WebClient + Mono.
 * Ante ENPROCESO, MS-Compra reagenda la validacion con @Scheduled (ver SnsPollingScheduler).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnsClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${sns.base-url:http://10.43.101.18:8090}")
    private String snsBaseUrl;

    @Value("${sns.codigo-aseguradora:ASEG001}")
    private String codigoAseguradora;

    @Value("${sns.timeout-ms:5000}")
    private long timeoutMs;

    public Mono<EstadoValidacionSns> validarPlan(String codigoPlan) {
        return webClientBuilder.baseUrl(snsBaseUrl).build()
                .get()
                .uri(uri -> uri.path("/api/sns/validar")
                        .queryParam("codigoPlan", codigoPlan)
                        .queryParam("codigoAseguradora", codigoAseguradora)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .map(body -> {
                    Object estado = body.get("estado");
                    if (estado == null) return EstadoValidacionSns.ENPROCESO;
                    return switch (estado.toString().toUpperCase()) {
                        case "APROBADO" -> EstadoValidacionSns.APROBADO;
                        case "RECHAZADO" -> EstadoValidacionSns.RECHAZADO;
                        default -> EstadoValidacionSns.ENPROCESO;
                    };
                })
                .onErrorResume(ex -> {
                    log.warn("SNS no disponible para plan {}: {} -> reintentar luego",
                            codigoPlan, ex.getMessage());
                    return Mono.just(EstadoValidacionSns.ENPROCESO);
                });
    }
}
