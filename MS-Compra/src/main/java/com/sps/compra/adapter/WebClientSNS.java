package com.sps.compra.adapter;

import com.sps.compra.entity.ValidacionSNS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Adapter HTTP asincrono hacia la SNS.
 * Encapsula las llamadas WebClient REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientSNS {

    private final WebClient.Builder webClientBuilder;

    @Value("${sns.base-url:http://10.43.101.18:8090}")
    private String snsBaseUrl;

    @Value("${sns.codigo-aseguradora:ASEG001}")
    private String codigoAseguradora;

    @Value("${sns.timeout-ms:5000}")
    private long timeoutMs;

    public Mono<ValidacionSNS> validarPlan(String codigoPlan) {
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
                    if (estado == null) return ValidacionSNS.ENPROCESO;
                    return switch (estado.toString().toUpperCase()) {
                        case "APROBADO" -> ValidacionSNS.APROBADO;
                        case "RECHAZADO" -> ValidacionSNS.RECHAZADO;
                        default -> ValidacionSNS.ENPROCESO;
                    };
                })
                .onErrorResume(ex -> {
                    log.warn("SNS no disponible para plan {}: {} -> reintentar luego",
                            codigoPlan, ex.getMessage());
                    return Mono.just(ValidacionSNS.ENPROCESO);
                });
    }
}
