package com.sps.compra.adapter;

import com.sps.compra.entity.ValidacionSNS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Adaptador reactivo (WebFlux) hacia el microservicio SNS.
 *
 * <p>Sigue el patron del slide 27 del pptx
 * {@code 07_spring_timer_p2p_microservicios_v2}: el {@link WebClient} se
 * construye una unica vez en el constructor a partir del {@code WebClient.Builder}
 * inyectado, y se reusa en cada llamada.</p>
 *
 * <p>El metodo {@link #validarPlan(String)} retorna un {@link Mono} con el
 * resultado de la validacion. Se aplican:</p>
 * <ul>
 *   <li>{@code .timeout(Duration)} — corta la llamada si SNS no responde a tiempo.</li>
 *   <li>{@code .onErrorResume(...)} — devuelve {@link ValidacionSNS#ENPROCESO}
 *       si SNS esta caido o devuelve error, permitiendo que el {@code TimerSNS}
 *       reintente la validacion mas tarde (resiliencia a nivel de proceso).</li>
 * </ul>
 *
 * <p>El manejo de errores diferencia el tipo de excepcion para que el log
 * sea util en sustentacion (timeout vs error HTTP vs error desconocido),
 * tal como muestra el pptx.</p>
 */
@Component
@Slf4j
public class WebClientSNS {

    /**
     * WebClient construido una sola vez con la base-url del SNS.
     * Patron del slide 27 del pptx (WebClient como field, no rebuild por llamada).
     */
    private final WebClient webClient;

    @Value("${sns.codigo-aseguradora:ASEG001}")
    private String codigoAseguradora;

    @Value("${sns.timeout-ms:5000}")
    private long timeoutMs;

    /**
     * Construye el WebClient con la baseUrl del SNS al iniciar la aplicacion.
     *
     * @param webClientBuilder builder global provisto por {@code WebClientConfig}
     * @param snsBaseUrl       URL base del microservicio SNS (configurable por env var)
     */
    public WebClientSNS(WebClient.Builder webClientBuilder,
                         @Value("${sns.base-url:http://10.43.101.18:8090}") String snsBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(snsBaseUrl).build();
    }

    /**
     * Valida un plan de salud contra el microservicio SNS de forma no-bloqueante.
     *
     * @param codigoPlan codigo del plan a validar (ej. {@code PLAN-BASICO-001})
     * @return {@code Mono<ValidacionSNS>} que emite el resultado cuando SNS responde,
     *         o {@code ENPROCESO} si hubo error (para que el TimerSNS reintente)
     */
    public Mono<ValidacionSNS> validarPlan(String codigoPlan) {
        return webClient.get()
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
                // Manejo de errores diferenciado por tipo (patron del slide 27 del pptx).
                // En los tres casos devolvemos ENPROCESO para que el TimerSNS reintente
                // mas tarde sin bloquear el flujo de la compra.
                .onErrorResume(e -> {
                    if (e instanceof TimeoutException) {
                        log.warn("Timeout en la llamada a SNS para plan {}", codigoPlan);
                    } else if (e instanceof WebClientResponseException wcre) {
                        log.warn("Error HTTP de SNS: {} para plan {}",
                                wcre.getStatusCode(), codigoPlan);
                    } else {
                        log.warn("Error desconocido en SNS para plan {}: {}",
                                codigoPlan, e.getMessage());
                    }
                    return Mono.just(ValidacionSNS.ENPROCESO);
                });
    }
}
